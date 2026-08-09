# Setup for Windows

## Installing WSL
Run cmd
- systeminfo
You should see the following:
```
VM Monitor Mode Extensions: Yes

Virtualization Enabled In Firmware: Yes

Second Level Address Translation: Yes

Data Execution Prevention Available: Yes
```
If you see no, then you will have to reboot and turn on virtualization in your bios.
This depends on the manufacturer, but for HP laptops, you can keep hitting esc to go into bios
Look for an option to turn on the virtualization, then save and exit.

Open powershell and install WSL (Ubuntu Linux by default)
wsl --install

You can optionally install the Windows terminal: https://apps.microsoft.com/detail/9n0dx20hk701

## Installing packages
As root, run the installRoot.sh (you will need the WSL password)
This installs the necessary packages
```
sudo -s
./installRoot.sh
```

Then exit and run the install script as the local user
```
./install.sh
```

Finally, copy and paste the bashrc_add.sh into the ~/.bashrc file
