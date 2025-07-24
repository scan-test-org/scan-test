FROM reg.docker.alibaba-inc.com/cnstack/cnstack-alios7u2:1.0-beta

COPY nginx.conf /etc/nginx/nginx.conf
COPY proxy.conf /etc/nginx/default.d/proxy.conf
COPY bin /home/admin/bin

RUN mkdir -p /app

WORKDIR /app

COPY dist/ /app

CMD ["/bin/bash", "/home/admin/bin/start.sh"]


#谐云环境dockerfile配置。。。
#FROM bitnami/nginx:1.18
#WORKDIR /app
#COPY nginx.conf /opt/bitnami/nginx/conf/nginx.conf
#COPY proxy.conf /opt/bitnami/nginx/conf/bitnami/my.conf
#COPY dist/ ./

#FROM bitnami/nginx:1.18
#WORKDIR /app
#ENV port=8000
#
#RUN sed -i -r "s|(\s+listen\s+)[0-9]+;|\1$port;|" /opt/bitnami/nginx/conf/nginx.conf
#COPY proxy.conf /opt/bitnami/nginx/conf/bitnami/my.conf
#COPY dist ./

