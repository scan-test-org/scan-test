FROM hub.docker.alibaba-inc.com/aone-base-global/alios7-nodejs:1.1

WORKDIR /app

COPY . .

RUN curl https://anpm.alibaba-inc.com/open/install-node.sh?v=18 | bash
RUN node -v
RUN npm -v

RUN npm install
RUN npm run build

EXPOSE 3000

CMD ["npm", "run", "start"]