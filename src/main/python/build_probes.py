#!/usr/bin/env python

import errno
import os
import shutil
import sys
import yaml

# Capture our current directory
THIS_DIR = os.path.dirname(os.path.abspath(__file__))
TEMPLATES_DIR = os.path.join(THIS_DIR, "..", "templates")
JAVA_DIR = os.path.join(THIS_DIR, "..", "java")
RESOURCES_DIR = os.path.join(THIS_DIR, "..", "resources")
LIB_DIR = os.path.join(THIS_DIR, "..", "..", "..", "lib")
TMP_DIR = os.path.join(JAVA_DIR, "tmp")

# Because we ship jinja2 with this project, we need to add it to the path
sys.path.append("%s/markupsafe" % LIB_DIR)
sys.path.append("%s/jinja2" % LIB_DIR)
from jinja2 import Environment, FileSystemLoader

# Create jinja2 environment
j2_env = Environment(loader=FileSystemLoader(TEMPLATES_DIR),
        trim_blocks=True)

# Make sure a directory exists
def mkdirp(path):
    try:
        os.makedirs(path)
    except OSError as exception:
        if exception.errno != errno.EEXIST:
            raise

# Create a probe aspect, which instrument a method
def build_probe(probe_name, method_signature, encoder):
    probe_dir = os.path.join(TMP_DIR, probe_name)
    mkdirp(probe_dir)
    with open(os.path.join(probe_dir, "ProbeAspect.java"), "w") as f:
        f.write(j2_env.get_template("probe_aspect.tmpl").render(
            probe_name=probe_name,
            method_signature=method_signature,
            encoder=encoder))

# Create a the aspectj config file for all probe aspects
def build_config(probe_names):
    aop_dir = os.path.join(RESOURCES_DIR, "META-INF")
    mkdirp(aop_dir)
    with open(os.path.join(aop_dir, "aop.xml"), "w") as f:
        f.write(j2_env.get_template("aop.tmpl").render(
            probe_names=probe_names))

if __name__ == "__main__":
    with open(os.path.join(RESOURCES_DIR, "probes_spec.yaml"), "r") as f:
        probes_spec = yaml.load(f)

        # Aspect source files are written to a temporary location
        shutil.rmtree(TMP_DIR)
        mkdirp(TMP_DIR)
        probe_names = []

        # Generate one source file aspect per probe
        for probe_name in probes_spec["probes"]:
            probe_names.append(probe_name)

            # Method to probe
            method = probes_spec["probes"][probe_name]["method"]

            # Argument types of this method
            args = probes_spec["probes"][probe_name].get("args", "")

            # Return type of this methos
            _return = probes_spec["probes"][probe_name].get("return", "")
            
            # An encoder can transform the method return. By default we have not
            # collateral effect with the default encoder 'encoder.Encoder'
            encoder = probes_spec["probes"][probe_name].get("encoder", "encoder.Encoder")

            # Particular case if willing to instrument a class constructor
            if not method.endswith(".new") and not _return:
                _return = "void"

            # Aspectj does not recognize the Unit definition and thus, using
            # 'void' is the right way to do it
            if _return == "scala.Unit":
                _return = "void"

            # Build Aspectj's expected method signature
            method_signature = "%s %s(%s)" % (_return, method, ",".join(args))

            # Build this probe, i.e., generate an annotated java class that
            # implements a probe aspect
            build_probe(probe_name, method_signature, encoder)

        # Build Aspectj config file for all probe aspects
        build_config(probe_names)
