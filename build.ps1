$FUNCTION_NAME = "aws-spring-sample"
$BUCKET = "satouryouhei-sample-bucket"
$KEY = "bookshelf-aws-spring.zip"
$ALIAS = "live"

mvn package -DskipTests
if ($LASTEXITCODE -ne 0) { exit 1 }

aws s3 cp target/$KEY s3://$BUCKET/$KEY
aws lambda update-function-code --function-name $FUNCTION_NAME --s3-bucket $BUCKET --s3-key $KEY
Write-Host "関数コードの更新完了を待っています..."
aws lambda wait function-updated --function-name $FUNCTION_NAME

Write-Host "バージョンを発行しています..."
$VER = aws lambda publish-version --function-name $FUNCTION_NAME --query Version --output text
Write-Host "バージョン $VER を発行しました。スナップショットの作成を待っています(数分かかる場合があります)..."
aws lambda wait published-version-active --function-name $FUNCTION_NAME --qualifier $VER
Write-Host "スナップショットの作成が完了しました。エイリアスを更新します。"
aws lambda update-alias --function-name $FUNCTION_NAME --name $ALIAS --function-version $VER
