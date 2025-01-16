FROM gradle:8.10.2
LABEL authors="Jawad Tahir"

COPY ./ /challenger

WORKDIR /challenger
VOLUME /data
EXPOSE 52923
RUN gradle clean build

ENTRYPOINT ["gradle", "run"]