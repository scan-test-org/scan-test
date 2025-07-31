set -e
rm -rf ./dist
npm run build

repository=
aliyun_user=mse_dev_chengpu_testcloud_com
aliyun_password=
namespace=daofeng
image=api-portal-frontend
version=1.0.0

docker buildx build -t $repository/$namespace/$image:$version --platform linux/amd64 -o type=docker .
docker login $repository --username $aliyun_user --password $aliyun_password
docker push $repository/$namespace/$image:$version
