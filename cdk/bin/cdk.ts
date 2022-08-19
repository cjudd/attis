#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import { AttisCdkStack } from '../lib/attis-cdk-stack';

const app = new cdk.App();
new AttisCdkStack(app, 'AttisStack', {

  env: { account: process.env.CDK_DEFAULT_ACCOUNT, region: process.env.CDK_DEFAULT_REGION },

});