import { Stack, StackProps } from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as ecsPatterns from 'aws-cdk-lib/aws-ecs-patterns';

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

    const loadBalancedFargateService = new ecsPatterns.ApplicationLoadBalancedFargateService(this, 'attis-service', {
      cluster,
      memoryLimitMiB: 2048,
      desiredCount: 1,
      cpu: 1024,
      taskImageOptions: {
        image: ecs.ContainerImage.fromRegistry("public.ecr.aws/g4v9z1z2/attis:latest"),
        containerPort:8080,
      },
      securityGroups: [securityGroup],
      assignPublicIp: true,
      publicLoadBalancer: true,
      listenerPort: 80,
      openListener: true,
      loadBalancerName: 'attis-lb',
    });

    loadBalancedFargateService.targetGroup.configureHealthCheck({
      path: '/images/attis.png',
    });

  }
}
