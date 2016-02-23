import os

from fabric.api import local, run, cd, env, lcd, task
from fabric.operations import put
from fabric.colors import green, red
from cwsfabric import config, to, portal

# Removes cached class files & runs ant to compile war
@task
def compile():
    local("rm -rf docroot/WEB-INF/classes && ant && date")

# Compiles and deploys war file to node
@task
def deploy():
    to.abort_if_no_environment()
    compile()
    portal.deploy_war(env.war_file, env.webapp_dir)

# Deploys the latest generated war file for ecs and deploys it to node
@task
def deploy_ecs():
    filename = get_latest_ecs_war()
    portal.deploy_war(filename, env.webapp_dir)
    print(green("Deployed " + filename))

# Removes unit test code, compiles/generate war file
@task
def build_ecs_war():
    remove_tests()
    print(green("Removed tests code from war"))
    compile()
    print(green("Generated war file"))
    restore_tests()
    print(green("Restored tests into code"))
    ecs_filename = get_file_name()
    with lcd(env.liferay_dist_dir):
        local("cp " + env.war_file + " " + ecs_filename)
    print(green("ECS File: " + env.liferay_dist_dir + ecs_filename))

def remove_tests():
    local("mv docroot/WEB-INF/src/edu/osu/cws/evals/tests/ /tmp/")

def restore_tests():
    local("mv /tmp/tests/ docroot/WEB-INF/src/edu/osu/cws/evals/tests/")

# Gets the current version of the app by reading version.txt
def get_version():
    version_file = file("VERSION.txt")
    version = version_file.read()
    version_file.close()
    version = version.strip()
    return version

# Generates the filename for ecs builds. It uses the rc or release suffix and the 
# prod_prefix. It uses a counter for the suffix, if we've already generated the war 
# for ecs for this version before.
def get_file_name():
    name_to_check = env.prod_prefix + get_version()
    if env.is_rc == True:
        name_to_check += "-rc" + get_rc_number(name_to_check)
    else:
        name_to_check += "-release" + get_release_number(name_to_check)
    
    name_to_check += ".war"
    return name_to_check

def get_rc_number(filename, increment=True):
    i = 1
    found_rc_number = False
    while found_rc_number == False:
        name_to_check =  filename + "-rc" + str(i) +  ".war"
        if os.path.isfile(env.liferay_dist_dir + name_to_check):
            i = i+1
        else:
            found_rc_number = True

    if increment == False and i > 1:
        i = i-1

    return str(i)

def get_release_number(filename, increment=True):
    i = 1
    found_rc_number = False
    while found_rc_number == False:
        name_to_check =  filename + "-release" + str(i) +  ".war"
        if os.path.isfile(env.liferay_dist_dir + name_to_check):
            i = i+1
        else:
            found_rc_number = True

    if increment == False and i > 1:
        i = i-1

    return str(i)
    
def get_latest_ecs_war():
    filename = env.prod_prefix + get_version()
    if env.is_rc == True:
        filename += "-rc" + get_rc_number(filename, False)
    else:
        filename += "-release" + get_release_number(filename, False)
    
    filename += ".war"
    return filename

@task
def prod():
    env.is_rc = False

# Setups liferay & evals dev environment
@task
def setup():
    portal.liferay_dev_setup()

    with lcd('~/Documents/liferay-plugins-sdk-5.2.3/portlets'):
        local('git clone git@gitlab.cws.oregonstate.edu:evals.git')
        with lcd('evals'):
            local('git checkout develop')

