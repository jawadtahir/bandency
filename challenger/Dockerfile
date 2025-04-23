FROM gradle:8.10.2
LABEL authors="Jawad Tahir"

COPY ./ /challenger

WORKDIR /challenger
VOLUME /data
EXPOSE 52923
ENV DB_CONNECTION="mongodb://localhost:27017/" REST_PORT="52923" DATA_DIR="/dataDir" DATA_DIR_EVAL="/dataDirEval"
RUN gradle clean build

ENTRYPOINT ["gradle", "run"]