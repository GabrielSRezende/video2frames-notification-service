#!/bin/sh
set -e

awslocal s3 mb s3://video2frames

awslocal sqs create-queue --queue-name video-uploaded
awslocal sqs create-queue --queue-name video-processed
awslocal sqs create-queue --queue-name video-failed

echo "Bucket e filas do video2frames criados no LocalStack."
