import webserver


def create_ns(namespace):
    webserver.create_quotas_in_Kubernetes(namespace)

if __name__ == "__main__":
    create_ns("group-1")