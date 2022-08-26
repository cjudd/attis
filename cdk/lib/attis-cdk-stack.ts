import { Stack, StackProps } from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as iam from 'aws-cdk-lib/aws-iam';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as ecsPatterns from 'aws-cdk-lib/aws-ecs-patterns';
import * as sm from 'aws-cdk-lib/aws-secretsmanager';

export class AttisCdkStack extends Stack {
  constructor(scope: Construct, id: string, props?: StackProps) {
    super(scope, id, props);

    const vpc = ec2.Vpc.fromLookup(this, "DefaultVPC", { isDefault: true});

    const cluster = new ecs.Cluster(this, 'AttisCluster', {
      clusterName: 'attis-cluster',
      containerInsights: true,

      vpc: vpc,
    });

    const securityGroup = ec2.SecurityGroup.fromLookupByName(this, 'dev-sg', 'devvm-default-sg', vpc);

    const secrets = sm.Secret.fromSecretCompleteArn(this, "attis-secrets",
        "arn:aws:secretsmanager:" + this.region +":" + this.account + ":secret:attis-JN36pR");

    const loadBalancedFargateService = new ecsPatterns.ApplicationLoadBalancedFargateService(this, 'attis-service', {
      cluster,
      memoryLimitMiB: 2048,
      desiredCount: 1,
      cpu: 1024,
      taskImageOptions: {
        image: ecs.ContainerImage.fromRegistry("public.ecr.aws/g4v9z1z2/attis:latest"),
        containerPort:8080,
        secrets: {
          'SPRING_MAIL_USERNAME': ecs.Secret.fromSecretsManager( secrets,'spring.mail.username'),
          'SPRING_MAIL_PASSWORD': ecs.Secret.fromSecretsManager( secrets, 'spring.mail.password'),
          'MESSAGE_SEND_FROM':ecs.Secret.fromSecretsManager( secrets, 'message.send.from')
        }
      },
      securityGroups: [securityGroup],
      assignPublicIp: true,
      publicLoadBalancer: true,
      listenerPort: 80,
      openListener: true,
      loadBalancerName: 'attis-lb',
    });

    loadBalancedFargateService.taskDefinition.addToTaskRolePolicy(new iam.PolicyStatement({
      effect: iam.Effect.ALLOW,
      actions: [
        'iam:CreateUser',
        'iam:TagUser',
        'iam:GetUser',
        'iam:CreateAccessKey',
        'iam:CreateLoginProfile',
        'iam:AddUserToGroup',
        'iam:ListAccountAliases',
        'ec2:RunInstances',
        'ec2:CreateTags',
        'secretsmanager:GetSecretValue'
      ],
      resources: ['*']
    }));

    loadBalancedFargateService.targetGroup.configureHealthCheck({
      path: '/images/attis.png',
    });

  }
}
