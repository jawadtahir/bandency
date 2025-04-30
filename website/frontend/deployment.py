import os
import subprocess
import shlex
import shutil
import logging
from os import path
from bson import ObjectId
from werkzeug.datastructures import FileStorage
from yaml import safe_load_all, safe_dump_all, YAMLError

logger = logging.getLogger("deployment")
DEPLOY_DIR = os.environ.get("DEPLOYMENT_DEPLOY_DIR", os.path.abspath("website/deploy"))
CHAOS_TEMPLATE_DIR = os.environ.get("CHAOS_TEMPLATE_DIR", os.path.abspath("website/kubernetes"))

def generate_chaos(chaos_type, namespace, id, timer_sec=1):
    if chaos_type == "pod":
        chaos_file = "podchaos.yaml"
    else:
        chaos_file = "networkchaos.yaml"

    chaos_file = os.path.join(CHAOS_TEMPLATE_DIR, chaos_file)

    with open(chaos_file) as chaos_template:

        chaos_yaml = chaos_template.read()
        chaos_yaml = chaos_yaml.format(deploy_id=id, namespace=namespace, timer=timer_sec)

        chaos_yaml = list(safe_load_all(chaos_yaml))[0]

        return chaos_yaml


class Deployment():
    
    upload_dir = os.environ.get("DEPLOYMENT_UPLOAD_DIR", os.path.abspath("upload"))
    

    def __init__(self, namespace, **deploy_args):

        self.namespace = namespace

        self._id = deploy_args.get("id", ObjectId())
        self.upoad_file_path = deploy_args.get("upload_file_path", None)
        self.is_failure = deploy_args.get("is_failure", False)

        if self.is_failure:
            self.timer_sec = deploy_args.get("timer", 1)
            self.chaos_type = deploy_args["failure_type"]
        

    def process_constraints(self):

        with open(self.upoad_file_path) as upload_file:
            try:
                workloads = list(safe_load_all(upload_file.read()))
            except YAMLError as err:
                logger.error(str(err))
                return (False, str(err))
        
        for workload in workloads:
            metadata = workload["metadata"]
            if "namespace" not in metadata.keys():
                return (False, "namespace not found")
            elif metadata["namespace"].strip() != self.namespace:
                return (False, "Invalid namespace")
            
        if self.is_failure:
            chaos_yaml = generate_chaos(self.chaos_type, self.namespace, self._id, self.timer_sec)
            workloads.append(chaos_yaml)

        deploy_file_path = os.path.join(DEPLOY_DIR, self.namespace, "{}.yaml".format(self._id))

        if not os.path.isdir(os.path.dirname(deploy_file_path)):
            os.makedirs(os.path.dirname(deploy_file_path))

        with open(deploy_file_path, "w") as deploy_file:

            safe_dump_all(workloads, deploy_file)

            return (True, "")


    def deploy(self):

        deploy_file_path = os.path.join(DEPLOY_DIR, self.namespace, "{}.yaml".format(self._id))

        result = subprocess.run("kubectl create -f {}".format(os.path.basename(deploy_file_path)), 
                                capture_output=True, 
                                cwd=os.path.dirname(deploy_file_path), 
                                shell=True)
        

        if result.returncode != 0:
            logger.error(result.stderr.decode("utf8"))
            return (False, result.stderr.decode("utf8"))
        else:
            return (True, result.stdout.decode("utf8"))
        

    def delete(self):

        deploy_file_path = os.path.join(DEPLOY_DIR, self.namespace, "{}.yaml".format(self._id))

        result = subprocess.run("kubectl delete -f {}".format(os.path.basename(deploy_file_path)), 
                                capture_output=True, 
                                cwd=os.path.dirname(deploy_file_path), 
                                shell=True)
        
        
        if result.returncode != 0:
            logger.error(result.stderr.decode("utf8"))
            return (False, result.stderr.decode("utf8"))
        else:
            return (True, result.stdout.decode("utf8"))
        

    def get_status(self):

        deploy_file_path = os.path.join(DEPLOY_DIR, self.namespace, "{}.yaml".format(self._id))

        result = subprocess.run("kubectl describe -f {} -o json".format(os.path.basename(deploy_file_path)), 
                                capture_output=True, 
                                cwd=os.path.dirname(deploy_file_path), 
                                shell=True)
        
        
        if result.returncode != 0:
            
            logger.error(result.stderr.decode("utf8"))
            return (False, result.stderr.decode("utf8"))
        else:
            return (True, result.stdout.decode("utf8"))



        

        

        
