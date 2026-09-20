mvn package -DskipTests
aws s3 rm s3://satouryouhei-sample-bucket/bookshelf-aws-spring.zip
aws s3 cp target/bookshelf-aws-spring.zip s3://satouryouhei-sample-bucket
aws lambda update-function-code --function-name aws-spring-sample --s3-bucket satouryouhei-sample-bucket --s3-key bookshelf-aws-spring.zip