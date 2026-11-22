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
adb install -r new/app-debug.apk || { echo "Falha ao instalar a versão nova por cima"; exit 1; }
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
if ! grep -q "^OK (" instrument.txt; then echo "O passeio de interface falhou"; exit 1; fi

# Tempo de abertura (debug) para comparação
adb shell am force-stop $PKG
echo "debug: $(adb shell am start -W -n $PKG/.MainActivity | grep TotalTime)" | tee shots/timing.txt

if [ -f new/app-release.apk ]; then
  echo "== Versão de publicação (R8/minificada): instalação, abertura e teste de estresse"
  adb uninstall $PKG >/dev/null
  adb install new/app-release.apk || { echo "Falha ao instalar a versão de publicação"; exit 1; }
  adb logcat -c
  adb shell am start -W -n $PKG/.MainActivity > /dev/null; sleep 6; adb shell am force-stop $PKG; sleep 2
  for i in 1 2 3; do
    echo "release frio $i: $(adb shell am start -W -n $PKG/.MainActivity | grep TotalTime)" | tee -a shots/timing.txt
    sleep 4; adb shell am force-stop $PKG; sleep 2
  done
  adb shell am start -W -n $PKG/.MainActivity > /dev/null; sleep 4
  adb shell monkey -p $PKG --pct-syskeys 0 --pct-appswitch 0 --throttle 120 -s 7 800 > shots/monkey.txt 2>&1 || true
  sleep 3
  adb exec-out screencap -p > shots/99-release.png || true
  if adb logcat -d | grep -E "FATAL EXCEPTION|ANR in $PKG" -A 40 > shots/release-crash.txt; then
    echo "A versão de publicação travou"; cat shots/release-crash.txt | head -80; exit 1
  fi
fi
exit 0
