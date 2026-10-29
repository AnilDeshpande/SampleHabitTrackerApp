#!/usr/bin/env bash
# Create an Android Virtual Device by writing its two definition files directly.
# Use this when the SDK's legacy avdmanager cannot run (it needs JAXB, removed from JDK 11+).
# The system image must already be installed: android sdk install "system-images;android-<api>;<tag>;<abi>"
# Usage: create-avd.sh <avd-name> <api> [--tag default|google_apis] [--device pixel_2] [--force]
# Honors ANDROID_SDK_ROOT / ANDROID_HOME, and ANDROID_AVD_HOME / ANDROID_USER_HOME for the AVD location.
set -euo pipefail

usage() { echo "usage: $(basename "$0") <avd-name> <api> [--tag default|google_apis] [--device pixel_2] [--force]" >&2; exit 2; }
[ $# -ge 2 ] || usage
name="$1"; api="$2"; shift 2
tag="default"; device="pixel_2"; force=0
while [ $# -gt 0 ]; do
  case "$1" in
    --tag) tag="${2:-}"; shift 2 ;;
    --device) device="${2:-}"; shift 2 ;;
    --force) force=1; shift ;;
    *) usage ;;
  esac
done
[[ "$name" =~ ^[A-Za-z0-9_.-]+$ ]] || { echo "invalid AVD name: $name" >&2; exit 2; }
[[ "$api" =~ ^[0-9]+$ ]] || { echo "invalid API level: $api" >&2; exit 2; }
[[ "$tag" =~ ^[a-z_]+$ && "$device" =~ ^[a-z0-9_]+$ ]] || usage

case "$(uname -m)" in
  arm64|aarch64) abi="arm64-v8a"; arch="arm64" ;;
  *) abi="x86_64"; arch="x86_64" ;;
esac

sdk="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}"
image_dir="system-images/android-${api}/${tag}/${abi}"
[ -d "$sdk/$image_dir" ] || {
  echo "system image not installed: $sdk/$image_dir" >&2
  echo "install it first: android sdk install \"system-images;android-${api};${tag};${abi}\"" >&2
  exit 1
}

avd_home="${ANDROID_AVD_HOME:-${ANDROID_USER_HOME:-$HOME/.android}/avd}"
avd_dir="$avd_home/$name.avd"
if { [ -e "$avd_dir" ] || [ -e "$avd_home/$name.ini" ]; } && [ "$force" -ne 1 ]; then
  echo "AVD '$name' already exists in $avd_home; refusing to overwrite (use --force only for an AVD you created)" >&2
  exit 1
fi

mkdir -p "$avd_dir"
cat > "$avd_home/$name.ini" <<INI
avd.ini.encoding=UTF-8
path=$avd_dir
path.rel=avd/$name.avd
target=android-$api
INI
case "$tag" in
  default) display="Default Android System Image" ;;
  google_apis) display="Google APIs" ;;
  *) display="$tag" ;;
esac
cat > "$avd_dir/config.ini" <<CONFIG
AvdId=$name
PlayStore.enabled=false
abi.type=$abi
avd.ini.displayname=$name
avd.ini.encoding=UTF-8
disk.dataPartition.size=4G
fastboot.forceColdBoot=yes
hw.accelerometer=yes
hw.audioInput=no
hw.battery=yes
hw.camera.back=none
hw.camera.front=none
hw.cpu.arch=$arch
hw.cpu.ncore=4
hw.dPad=no
hw.device.manufacturer=Google
hw.device.name=$device
hw.gps=no
hw.gpu.enabled=yes
hw.gpu.mode=auto
hw.initialOrientation=portrait
hw.keyboard=yes
hw.lcd.density=420
hw.lcd.height=1920
hw.lcd.width=1080
hw.mainKeys=no
hw.ramSize=2048
hw.sdCard=no
hw.trackBall=no
image.sysdir.1=$image_dir/
runtime.network.latency=none
runtime.network.speed=full
showDeviceFrame=no
tag.display=$display
tag.id=$tag
vm.heapSize=228
CONFIG
echo "created AVD '$name' (API $api, $tag, $abi) in $avd_home"
echo "verify: ANDROID_AVD_HOME='$avd_home' \"$sdk/emulator/emulator\" -list-avds"
