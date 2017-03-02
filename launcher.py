#! /usr/bin/env python

import subprocess
import argparse
import time
import os

if __name__ == "__main__":
    programs = []
    parser = argparse.ArgumentParser()

    parser.add_argument("-s", "--servers", nargs='?', type=int, default=0, const=1,
                        help="Specifies number of servers to start")
    parser.add_argument("-c", "--clients", nargs='?', type=int, default=0, const=1,
                        help="Specifies number of clients to start")
    parser.add_argument("-o", "--outputs_in_files", action="store_true",
                        help="Stores each program's output in .out, .err files")


    args = parser.parse_args()

    project_dir = os.path.dirname(os.path.abspath(__file__))

    classpaths = ["{0}/sip-proxy/target/classes:{0}/sip-proxy/lib/*".format(project_dir),
              "{0}/sip-communicator/target/classes:{0}/sip-communicator/lib/*".format(project_dir)]
    programs = ["server", "client"]

    for i in xrange(args.servers):
        redirection = "1>../server%d.out 2>../server%d.err" % (i, i) if args.outputs_in_files else ""
        programs.append(subprocess.Popen(
            "cd sip-proxy && java -classpath %s gov.nist.sip.proxy.gui.ProxyLauncher %s" % (classpaths[0], redirection), shell=True))
    for i in xrange(args.clients):
        redirection = "1>../client%d.out 2>../client%d.err" % (i, i) if args.outputs_in_files else ""
        programs.append(subprocess.Popen(
            "cd sip-communicator && java -classpath %s net.java.sip.communicator.SipCommunicator %s" % (classpaths[1], redirection), shell=True))

    if len(programs) > 0:
        print "Programs started. Press ^C to end everything..."
        try:
            time.sleep(100000)
        except KeyboardInterrupt:
            print "Bye!"
    else:
        parser.print_help()


