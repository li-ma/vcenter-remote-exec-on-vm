package com.nodinator;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import com.vmware.vim25.GuestProgramSpec;
import com.vmware.vim25.NamePasswordAuthentication;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.GuestOperationsManager;
import com.vmware.vim25.mo.GuestProcessManager;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class GuestRunProgram {

	// VM default root credentials
	private static String VM_USERNAME = "root";
	private static String VM_PASSWORD = "vagrant";

	public String vCenterIp;
	public String vCenterUsername;
	public String vCenterPassword;
	public ServiceInstance vCenterConnection;

	public GuestRunProgram(String vCenterIp, String vCenterUsername, String vCenterPassword) throws RemoteException, MalformedURLException{
		this.vCenterIp = vCenterIp;
		this.vCenterUsername = vCenterUsername;
		this.vCenterPassword = vCenterPassword;
		this.vCenterConnection = new ServiceInstance(new URL("https://"+this.vCenterIp+"/sdk"), this.vCenterUsername, this.vCenterPassword, true);
		System.out.println("Success: Connection established with vCenter " + this.vCenterIp);
	}


	public VirtualMachine getVirtualMachine(String vmName) throws RemoteException, MalformedURLException {
		Folder rootFolder = this.vCenterConnection.getRootFolder();
		InventoryNavigator inv = new InventoryNavigator(rootFolder);
		VirtualMachine vm = (com.vmware.vim25.mo.VirtualMachine) inv.searchManagedEntity("VirtualMachine", vmName);
		if (vm != null) {
			System.out.println("Success: on vCenter " + this.vCenterIp + ", VM found: " + vmName);
		}
		return vm;
	}

	public boolean areVmwareToolsrunning(VirtualMachine vm) {
		if(!"guestToolsRunning".equals(vm.getGuest().toolsRunningStatus)){
			System.out.println("The VMware Tools is not running in the Guest OS on VM: " + vm.getName());
			System.out.println("Exiting...");
			return false;
		} else {
			System.out.println("Great, the VMware Tools are running in the Guest OS on VM: " + vm.getName());
			return true;
		}
	}


	/*	
	 *	Execute a command (based on bin path & args) on a VM triggered through vCenter
	 */	
	public void executeCommandOnVM(VirtualMachine vm, String commandProgramPath, String commandArgs) throws Exception {

		// Define the command to run of the VM
		GuestProgramSpec spec = new GuestProgramSpec();
		spec.programPath = commandProgramPath;
		spec.arguments = commandArgs;

		// Define ProcessManager for the target VM
		GuestOperationsManager gom = this.vCenterConnection.getGuestOperationsManager();
		GuestProcessManager gpm = gom.getProcessManager(vm);

		// Define Credentials for log into the target VM
		NamePasswordAuthentication npa = new NamePasswordAuthentication();
		npa.username = VM_USERNAME;
		npa.password = VM_PASSWORD;

		// Run the command defined on the target VM
		long pid = gpm.startProgramInGuest(npa, spec);
		System.out.println("Success: Command ran on VM " + vm.getName() + " is: '" + commandProgramPath + " " + commandArgs + "', with pid: " + pid);

		// Close the connection with vCenter
		this.vCenterConnection.getServerConnection().logout();	
		System.out.println("Success: Connection closed with vCenter " + this.vCenterIp);
	}



	public static void main(String[] args) {

		if (args.length != 6) {
			System.out.println("You need to pass 6 arguments: vcenter_ip, vcenter_username, vcenter_password, VM_name, bin_path, run_args");
			return;
		}

		String vcenterIp = args[0];
		String vcenterUsername = args[1];
		String vcenterPassword = args[2];
		String targetVmName = args[3];
		String commandBinPath = args[4];
		String commandArgs = args[5];

		GuestRunProgram gm = null;
		try {
			gm = new GuestRunProgram(vcenterIp, vcenterUsername, vcenterPassword);
						
		} catch (RemoteException | MalformedURLException e1) {
			System.out.println("Error: connection to vCenter " + vcenterIp + " failed");
			return;			
		}

		// Get the target VM object
		VirtualMachine targetVm = null;
		try {
			targetVm = gm.getVirtualMachine(targetVmName);
			if (targetVm == null) { throw new RemoteException(); }
		} catch (RemoteException | MalformedURLException e2) {
			System.out.println("Error: couldn't find target VM " + targetVmName + "on vCenter " + vcenterIp);
			return;
		}

		// Check if the VMware Tools are running
		boolean vmwareToolsRunning = gm.areVmwareToolsrunning(targetVm);

		// Execute command on target VM
		if (vmwareToolsRunning) {
			try {
				gm.executeCommandOnVM(targetVm, commandBinPath, commandArgs);
			} catch (Exception e3) {
				System.out.println("Error: Command failed on VM " + targetVmName + ": '" + commandBinPath + " " + commandArgs + "'");
				return;
			}			
		} else {
			return;
		}

	}
}