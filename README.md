# 1. Update Termux and grant storage access
pkg update -y && pkg upgrade -y
termux-setup-storage

# 2. Install OpenJDK 17, Gradle, aapt, and build utilities
pkg install -y openjdk-17 gradle git wget unzip tar aapt

# 3. Setup Android SDK Command Line Tools
export ANDROID_HOME=$HOME/android-sdk
export ANDROID_SDK_ROOT=$HOME/android-sdk
mkdir -p $ANDROID_SDK_ROOT/cmdline-tools

cd $ANDROID_SDK_ROOT/cmdline-tools
wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline-tools.zip
unzip -q cmdline-tools.zip
rm -rf latest
mv cmdline-tools latest
rm -f cmdline-tools.zip

# 4. Save environment variables permanently to bashrc
grep -q "ANDROID_HOME" ~/.bashrc || cat << 'EOF' >> ~/.bashrc
export ANDROID_HOME=$HOME/android-sdk
export ANDROID_SDK_ROOT=$HOME/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
EOF
source ~/.bashrc

# 5. Accept Android SDK licenses
yes | sdkmanager --licenses > /dev/null 2>&1

# 6. Extract the exported project from phone Downloads folder
mkdir -p ~/projects
cd ~/projects
cp ~/storage/downloads/investpro*.zip ./investpro.zip 2>/dev/null || true
unzip -o -q investpro.zip -d investpro 2>/dev/null || true
cd ~/projects/investpro

# 7. Configure low-memory settings for phone compilation
echo "org.gradle.jvmargs=-Xmx1024m -XX:MaxMetaspaceSize=512m" >> gradle.properties

# 8. Build the InvestPro Debug APK
gradle :app:assembleDebug -Dandroid.aapt2FromMavenOverride=/data/data/com.termux/files/usr/bin/aapt2 --stacktrace

# 9. Copy the generated APK to phone Downloads folder and launch installer
cp -f app/build/outputs/apk/debug/app-debug.apk ~/storage/downloads/InvestPro-Debug.apk
termux-open ~/storage/downloads/InvestPro-Debug.apk
