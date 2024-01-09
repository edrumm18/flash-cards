# This script starts wireless ADB on your connected device.
# Only one device should be plugged in by USB at a time.

# The port we will start adb on, on the device.
ADB_PORT='5555'

# Get the wlan IP address of the connected device.
OUTPUT=$(adb shell ip addr show wlan0 | grep "inet\s" | awk '{print $2}' | awk -F'/' '{print $1}')

# Start ADB on the connected device's port.
adb tcpip 5555

# Connect to the port that is running ADB on the device at the given IP address.
adb connect "${OUTPUT}":$ADB_PORT

