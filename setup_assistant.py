#! /usr/bin/env python

import subprocess
import sys
import os

HEADER = '\033[95m'
OKBLUE = '\033[94m'
OKGREEN = '\033[92m'
WARNING = '\033[93m'
FAIL = '\033[91m'
ENDC = '\033[0m'
BOLD = '\033[1m'
UNDERLINE = '\033[4m'

project_dir = os.path.dirname(os.path.abspath(__file__))


def cprint(color, string):
    print(color + string + ENDC)


def ask_call(command, silent=False, return_output=False):
    if not silent:
        res = raw_input("Allow me to run the following command:\n\t" + command + "\n?(y[ay], n[ope], q[uit]) Default = y: ")[0:1].lower()
        if res == "n": return None
        if res == "q": sys.exit(0)


    call = subprocess.check_output if return_output else subprocess.check_call
    try:
        return "c", call(command + " 2>&1", shell=True)
    except subprocess.CalledProcessError as e:
        return "f", e.returncode



if __name__ == "__main__":

    print("________________________________________")
    print("< Welcome to Sip Project Setup Assistant >")
    print(" ----------------------------------------")
    print("   \\")
    print("    \\")
    print("        .--.")
    print("       |o_o |")
    print("       |:_/ |")
    print("      //   \\ \\")
    print("     (|     | )")
    print("    /'\_   _/`\\")
    print("    \\___)=(___/")
    print("")
    print("This script will guide you through the required steps to run the sip proxy and client")
    print("")

    cprint(BOLD, "Step 1: Install Java Compiler 1.7 or greater")
    t, r = ask_call("javac -version", True, True)
    if t == "c" and r.split(" ")[-1][0:3] >= "1.7":
        cprint(OKGREEN, "Java Compiler Exists")
    else:
        cprint(WARNING, "Java Compiler Doesn't Exist. Trying to install...")
        v = "7" if raw_input("Do you like 7 or 8? ")[0:1] == "7" else "8"
        ask_call("sudo apt-get install openjdk-%s-jdk" % v)

    cprint(BOLD, "Step 2: Install Ant")
    t, _ = ask_call("ant -version", True, True)
    if t == "c":
        cprint(OKGREEN, "Ant Exists")
    else:
        cprint(WARNING, "Ant Doesn't Exist. Trying to install...")
        ask_call("sudo apt-get install ant")

    cprint(BOLD, "Step 3: Install MySQL")
    t, _ = ask_call("mysql --version", True, True)
    if t == "c":
        cprint(OKGREEN, "MySQL Exists")
    else:
        cprint(WARNING, "MySQL Doesn't Exist. Trying to install...")
        ask_call("sudo apt-get install mysql-server")

    while 1:
        cprint(BOLD, "Step 4: Create Database User and Database, use the password you provided during mysql installation.")
        ret = ask_call("mysql -u root -f -p < user_creator.mysql")
        if not ret or ret[0] == "c":
            break
        cprint(FAIL, "Command seems to have failed. Trying again...")

    while 1:
        cprint(BOLD, "Step 5: Create Database Tables, use the following password: \n\tsip")
        ret = ask_call("mysql -u sip_user -p < database_creator.mysql")
        if not ret or ret[0] == "c":
            break
        cprint(FAIL, "Command seems to have failed. Trying again...")

    cprint(BOLD, "Step 6: Build Proxy")
    ret = ask_call("cd sip-proxy && ant")
    if ret and ret[0] != "c":
        cprint(FAIL, "Command seems to have failed. Something probably failed during installation...")
        system.exit(1)

    cprint(BOLD, "Step 7: Build Client")
    ret = ask_call("cd sip-communicator && ant")
    if ret and ret[0] != "c":
        cprint(FAIL, "Command seems to have failed. Something probably failed during installation...")
        system.exit(1)

    cprint(BOLD, "Step 8: Edit Configuration Files")
    print("Edit the following configuration files:")
    print("\t" + project_dir + "/sip-proxy/src/gov/nist/sip/proxy/configuration/configuration.xml")
    print("\t" + project_dir + "/sip-proxy/target/classes/gov/nist/sip/proxy/configuration/configuration.xml")
    print("\t" + project_dir + "/sip-communicator/sip-communicator.xml")
    print("")
    print("according to the guide here (page 11 for files 1, 2, page 13 for file 3): ")
    print(r"http://mycourses.ntua.gr/document/goto/?url=%2F%C4%E9%E1%EB%DD%EE%E5%E9%F2%2FLecture3-Set2-Project-EclipseGitSetUp.ppt&cidReq=ECE1242")
    print("")
    print("When you are done with this step, you are probably ready to launch the project. Run ./launcher.py for more info")






