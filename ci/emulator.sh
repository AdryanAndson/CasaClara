#!/bin/bash
# Roda dentro do emulador do GitHub Actions.
#  1) instala a versão ANTIGA (banco v1) e abre o app, para criar dados no formato antigo;
#  2) instala a versão NOVA por cima e confere que abre sem travar (migração do banco);
#  3) roda o passeio de interface e copia os prints.
set -u
PKG=br.com.casaclara
adb logcat -c
if [ -f old/app-debug.apk ]; then
  echo "== Instalando versão antiga"
  adb install -r old/app-debug.apk
  adb shell am start -W -n $PKG/.MainActivity
  sleep 12
  adb shell am force-stop $PKG
fi
echo "== Instalando versão nova por cima"
adb install -r new/app-debug.apk
adb logcat -c
adb shell am start -W -n $PKG/.MainActivity
sleep 12
if adb logcat -d | grep -E "FATAL EXCEPTION" -A 30; then
  echo "O app travou ao abrir a versão nova"; adb logcat -d | tail -200; exit 1
fi
adb shell am force-stop $PKG
adb shell pm clear $PKG
echo "== Passeio de interface"
adb install -r new/app-debug-androidTest.apk
adb shell am instrument -w -e class br.com.casaclara.TourTest $PKG.test/androidx.test.runner.AndroidJUnitRunner | tee instrument.txt
mkdir -p shots
for f in $(adb shell run-as $PKG ls files/shots 2>/dev/null | tr -d '\r'); do
  adb exec-out run-as $PKG cat files/shots/$f > shots/$f
done
ls -la shots
adb logcat -d | grep -E "FATAL EXCEPTION" -A 30 > crash.txt || true
if grep -qE "FAILURES!!!|INSTRUMENTATION_FAILED|Process crashed" instrument.txt; then exit 1; fi
exit 0
