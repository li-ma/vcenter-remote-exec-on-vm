# remote-exec-on-vm
Remotely execute a command on a VM by triggering this command from the vCenter.

Usage:


Get 2 external jars:
* dom4j-1.6.1.jar
* vijava55b20130927.jar
=> download the zip file [http://sourceforge.net/projects/vijava/?source=typ_redirect here] which contains those 2 jars.

Export the Java class as a runnable jar file.

List of 6 arguments:
* vcenterIp
* vcenterUsername
* vcenterPassword
* targetVmName
* commandBinPath
* commandArgs

Run:
````
$ java -jar remote_exec_cmd_on_vm.jar \
"10.145.6.20" \
"root" \
"******" \
"d1p3920tlm-prxs-sc-a.vchslabs.vmware.com" \
"/bin/touch" \
"/home/foobar"

Success: Connection established with vCenter 10.145.6.20
Success: on vCenter 10.145.6.20, VM found: d1p3920tlm-prxs-sc-a.vchslabs.vmware.com
Great, the VMware Tools are running in the Guest OS on VM: d1p3920tlm-prxs-sc-a.vchslabs.vmware.com
Success: Command ran on VM d1p3920tlm-prxs-sc-a.vchslabs.vmware.com is: '/bin/touch /home/foobar', with pid: 8529
Success: Connection closed with vCenter 10.145.6.20
````
