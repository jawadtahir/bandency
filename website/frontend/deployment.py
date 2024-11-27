import os
from os import path
from bson import ObjectId
from werkzeug.datastructures import FileStorage
from yaml import safe_load, safe_dump


class Deployment():
    
    upload_dir = os.environ.get("DEPLOYMENT_UPLOAD_DIR", os.path.abspath("upload"))
    deploy_dir = os.environ.get("DEPLOYMENT_DEPLOY_DIR", os.path.abspath("deploy"))

    async def __init__(self, file: FileStorage):


        self._id = ObjectId()
        self.upload_file_name = os.path.join(self.upload_dir, "{}-{}".format(str(self._id), file.filename))
        self.deploy_file_name = os.path.join(self.deploy_dir, "{}-{}".format(str(self._id), file.filename))
        with open(self.upload_file_name, 'w') as fd:
            await file.save(fd)
        

    def process_constraints(self):
        self.upload_file = safe_load(self.upload_file_name)

        if len(self.upload_file["services"]) == 0:
            raise SyntaxError("No services found")
    



    def deploy(self):
        pass