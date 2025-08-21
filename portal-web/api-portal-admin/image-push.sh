set -e
rm -rf ./dist
npm run build

repository=registry.cn-shanghai.aliyuncs.com
aliyun_user=mse_dev_chengpu_testcloud_com
aliyun_password=AliCnstack%1688
namespace=daofeng
image=api-portal-admin

#repository=registry.cn-hangzhou.aliyuncs.com
#aliyun_user=sunxiaohan.sxh@1760720386195983
#aliyun_password=Serverless_123
#namespace=sae-dev-test
#image=api-portal-admin
version=1.0.0

docker buildx build -t $repository/$namespace/$image:$version --platform linux/amd64 -o type=docker .
docker login $repository --username $aliyun_user --password $aliyun_password
docker push $repository/$namespace/$image:$version

