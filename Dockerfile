# 多阶段构建 - 构建阶段
FROM hub.docker.alibaba-inc.com/aone-base-global/alios7-nodejs:1.1

WORKDIR /app

COPY . .

RUN curl https://anpm.alibaba-inc.com/open/install-node.sh?v=20 | bash

# 不知道为啥 在docker里npm install 会报错 所以暂时先直接copy本地build好的dist文件

EXPOSE 3000
CMD ["node", "server.js"]