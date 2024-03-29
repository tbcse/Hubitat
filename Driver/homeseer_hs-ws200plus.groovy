/**
 *  HomeSeer HS-WS200+ Hubitat
 *
 *  Copyright 2019 tbcse (Hubitat Port)
 *  Copyright 2018 HomeSeer (Modified for WS200+ - ST Driver)
 *  Copyright 2016, 2017 DarwinsDen.com (WD100+ - ST Driver)
 *
 *  Modified from the work by DarwinsDen device handler for the WD100 (v1.03) and from the work by HomeSeer for the HS-WS200+ (v1.0)
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Author: tbcse
 *  Date: 12/2019
 *
 *  Author: HomeSeer
 *  Date: 12/2017
 *
 *  Author: Darwin@DarwinsDen.com
 *  Date: 2016-04-10
 *
 *  Changelog:
 *  0.2       2019-12-20 1) Removed left over dimming preferences/code 2) Cleanup unused LEDs code 3) Locator Light tweak
 *  0.1*      2019-12-21 Initial Version. Ported to Hubitat from Homeseer's SmartThings driver (v 1.0) by semi-automatic merging from
 *              "HS-WD200+ Dimmer" driver by "codahq-hubitat" / "Ben Rimmasch". Merged blink code
 *
 *  * indicates internal/unpublished code
 *
 *   Button Mappings:
 *
 *   ACTION          BUTTON#    BUTTON ACTION
 *   Double-Tap Up     1        pressed
 *   Double-Tap Down   2        pressed
 *   Triple-Tap Up     3        pressed
 *   Triple-Tap Down   4        pressed
 *   Hold Up           5        pressed
 *   Hold Down         6        pressed
 *   Single-Tap Up     7        pressed
 *   Single-Tap Down   8        pressed
 *   4 taps up         9        pressed
 *   4 taps down       10       pressed
 *   5 taps up         11       pressed
 *   5 taps down       12       pressed
 *
 */

metadata {
    definition (name: "Homeseer WS200+ Switch", namespace: "tbcse", author: "tbcse") {
        capability "Actuator"
        capability "Switch"
        capability "Refresh"
        capability "Sensor"
        capability "PushableButton"
        capability "Configuration"

        command "tapUp2"
        command "tapDown2"
        command "tapUp3"
        command "tapDown3"
        command "tapUp4"
        command "tapDown4"
        command "tapUp5"
        command "tapDown5"
        command "holdUp"
        command "holdDown"
        command "setStatusLed", [
            [name: "Color*", type: "NUMBER", range: 0..7, description: "0=Off, 1=Red, 2=Green, 3=Blue, 4=Magenta, 5=Yellow, 6=Cyan, 7=White"],
            [name: "Blink?*", type: "NUMBER", range: 0..1, description: "0=No, 1=Yes", default: 0]
        ]
        command "setSwitchModeNormal"
        command "setSwitchModeStatus"
        command "locatorLightOn"
        command "locatorLightOff"
        command "setDefaultColor", [[name: "Set Normal Mode LED Color", type: "NUMBER", range: 0..6, description: "0=White, 1=Red, 2=Green, 3=Blue, 4=Magenta, 5=Yellow, 6=Cyan"]]
        command "setBlinkDurationMilliseconds ", [[name: "Set Blink Duration", type: "NUMBER", description: "Milliseconds (0 to 25500)"]]

        fingerprint mfr: "000C", prod: "4447", model: "3035"
    }

    simulator {
        status "on":  "command: 2003, payload: FF"
        status "off": "command: 2003, payload: 00"

        // reply messages
        reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
        reply "200100,delay 5000,2602": "command: 2603, payload: 00"
    }

    preferences {
       input "reverseSwitch", "bool", title: "Reverse Switch",  defaultValue: false,  displayDuringSetup: true, required: false
       input "locatorlight", "bool", title: "Locator Light When Load is Off",  defaultValue: false,  displayDuringSetup: true, required: false
       input ( "color", "enum", title: "Default LED Color", options: ["White", "Red", "Green", "Blue", "Magenta", "Yellow", "Cyan"], description: "Select Color", required: false)
       input name: "descriptionTextEnable", type: "bool", title: "Enable descriptionText logging", defaultValue: false
       input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: false
       input name: "traceLogEnable", type: "bool", title: "Enable trace logging", defaultValue: false
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute("device.status", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'${currentValue}', unit:"")
            }
        }

        standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.configure"
        }

        valueTile("firmwareVersion", "device.firmwareVersion", width:2, height: 2, decoration: "flat", inactiveLabel: false) {
            state "default", label: '${currentValue}'
        }

        standardTile("tapUp2", "device.button", width: 2, height: 2, decoration: "flat") {
            state "default", label: "Tap ▲▲", backgroundColor: "#ffffff", action: "tapUp2", icon: "st.Home.home30"
        }

        standardTile("tapDown2", "device.button", width: 2, height: 2, decoration: "flat") {
            state "default", label: "Tap ▼▼", backgroundColor: "#ffffff", action: "tapDown2", icon: "st.Home.home30"
        }

        standardTile("tapUp3", "device.button", width: 2, height: 2, decoration: "flat") {
            state "default", label: "Tap ▲▲▲", backgroundColor: "#ffffff", action: "tapUp3", icon: "st.Home.home30"
        }

        standardTile("tapDown3", "device.button", width: 2, height: 2, decoration: "flat") {
            state "default", label: "Tap ▼▼▼", backgroundColor: "#ffffff", action: "tapDown3", icon: "st.Home.home30"
        }

        standardTile("tapUp4", "device.button", width: 2, height: 2, decoration: "flat") {
            state "default", label: "Tap ▲▲▲▲", backgroundColor: "#ffffff", action: "tapUp4", icon: "st.Home.home30"
        }

        standardTile("tapDown4", "device.button", width: 2, height: 2, decoration: "flat") {
            state "default", label: "Tap ▼▼▼▼", backgroundColor: "#ffffff", action: "tapDown4", icon: "st.Home.home30"
        }

        standardTile("tapUp5", "device.button", width: 2, height: 2, decoration: "flat") {
            state "default", label: "Tap ▲▲▲▲▲", backgroundColor: "#ffffff", action: "tapUp5", icon: "st.Home.home30"
        }

        standardTile("tapDown5", "device.button", width: 2, height: 2, decoration: "flat") {
            state "default", label: "Tap ▼▼▼▼▼", backgroundColor: "#ffffff", action: "tapDown5", icon: "st.Home.home30"
        }

        standardTile("holdUp", "device.button", width: 2, height: 2, decoration: "flat") {
            state "default", label: "Hold ▲", backgroundColor: "#ffffff", action: "holdUp", icon: "st.Home.home30"
        }

        standardTile("holdDown", "device.button", width: 2, height: 2, decoration: "flat") {
            state "default", label: "Hold ▼", backgroundColor: "#ffffff", action: "holdDown", icon: "st.Home.home30"
        }

        main(["switch"])
        details(["switch","tapUp2","tapUp3","tapUp4","tapUp5","holdUp","tapDown2","tapDown3","tapDown4","tapDown5","holdDown","firmwareVersion", "refresh"])
    }
}

def parse(String description) {
    def result = null
    logDebug("parse($description)")
    if (description != "updated") {
        def cmd = zwave.parse(description, [0x20: 1, 0x70: 1])
        logTrace "cmd: $cmd"
        if (cmd) {
            result = zwaveEvent(cmd)
        }
    }
    if (!result){
        log.warn "Parse returned ${result} for command ${cmd}"
    }
    else {
        logDebug "Parse returned ${result}"
    }
    return result
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd) {
    createEvent([name: "switch", value: cmd.value ? "on" : "off", type: "physical"])
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicSet cmd) {
    createEvent([name: "switch", value: cmd.value ? "on" : "off", type: "physical"])
}

def zwaveEvent(hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    createEvent([name: "switch", value: cmd.value ? "on" : "off", type: "digital"])
}

def zwaveEvent(hubitat.zwave.commands.configurationv1.ConfigurationReport cmd) {
    logDebug "zwaveEvent(hubitat.zwave.commands.configurationv1.ConfigurationReport cmd)"
    logTrace "cmd: $cmd"
    def value = "when off"
    if (cmd.configurationValue[0] == 1) {value = "when on"}
    if (cmd.configurationValue[0] == 2) {value = "never"}
    logInfo "Indicator is on for fan: ${value}"
    createEvent([name: "indicatorStatus", value: value])
}

def zwaveEvent(hubitat.zwave.commands.hailv1.Hail cmd) {
    logDebug "zwaveEvent(hubitat.zwave.commands.hailv1.Hail cmd)"
    logTrace "cmd: $cmd"
    logInfo "Switch button was pressed"
    createEvent([name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false])
}

def zwaveEvent(hubitat.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    logDebug "zwaveEvent(hubitat.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd)"
    logTrace "cmd: $cmd"
    logDebug "manufacturerId:   ${cmd.manufacturerId}"
    logDebug "manufacturerName: ${cmd.manufacturerName}"
    logDebug "productId:        ${cmd.productId}"
    logDebug "productTypeId:    ${cmd.productTypeId}"
    def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
    def cmds = []
    if (!(msr.equals(getDataValue("MSR")))) {
        updateDataValue("MSR", msr)
        cmds << createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: true, displayed: false])
    }
    if (!(cmd.manufacturerName.equals(getDataValue("manufacturer")))) {
        updateDataValue("manufacturer", cmd.manufacturerName)
        cmds << createEvent([descriptionText: "$device.displayName manufacturer: $msr", isStateChange: true, displayed: false])
    }
    cmds
}

def zwaveEvent(hubitat.zwave.commands.versionv1.VersionReport cmd) {
    logDebug "zwaveEvent(hubitat.zwave.commands.versionv1.VersionReport cmd)"
    logTrace "cmd: $cmd"
    logDebug("received Version Report")
    logDebug "applicationVersion:      ${cmd.applicationVersion}"
    logDebug "applicationSubVersion:   ${cmd.applicationSubVersion}"
    logDebug "zWaveLibraryType:        ${cmd.zWaveLibraryType}"
    logDebug "zWaveProtocolVersion:    ${cmd.zWaveProtocolVersion}"
    logDebug "zWaveProtocolSubVersion: ${cmd.zWaveProtocolSubVersion}"
    def ver = cmd.applicationVersion + '.' + cmd.applicationSubVersion
    def cmds = []
    if (!(ver.equals(getDataValue("firmware")))) {
        updateDataValue("firmware", ver)
        cmds << createEvent([descriptionText: "Firmware V" + ver, isStateChange: true, displayed: false])
    }
    cmds
}

def zwaveEvent(hubitat.zwave.commands.firmwareupdatemdv2.FirmwareMdReport cmd) {
    log.debug ("received Firmware Report")
    log.debug "checksum:       ${cmd.checksum}"
    log.debug "firmwareId:     ${cmd.firmwareId}"
    log.debug "manufacturerId: ${cmd.manufacturerId}"
    [:]
}

def zwaveEvent(hubitat.zwave.Command cmd) {
    // Handles all Z-Wave commands we aren't interested in
    logDebug "zwaveEvent(hubitat.zwave.Command cmd)"
    logTrace "cmd: $cmd"
    [:]
}

def on() {
    logDebug "on()"
    sendEvent(tapUp1Response("digital"))
    delayBetween([
        zwave.basicV1.basicSet(value: 0xFF).format(),
        zwave.switchBinaryV1.switchBinaryGet().format()
    ])
}

def off() {
    logDebug "off()"
    sendEvent(tapDown1Response("digital"))
    delayBetween([
        zwave.basicV1.basicSet(value: 0x00).format(),
        zwave.switchBinaryV1.switchBinaryGet().format()
    ])
}

  def setBlinkDurationMilliseconds (newBlinkDuration) {
    logDebug "setBlinkDurationMilliseconds ($newBlinkDuration)"
    def cmds = []
    if (0 <= newBlinkDuration && newBlinkDuration <= 25500) {
      logDebug "setting blink duration to: ${newBlinkDuration} ms"
      state.blinkDuration = newBlinkDuration.toInteger() / 100
    } else {
      log.warn "commanded blink duration ${newBlinkDuration} is outside range 0 - 25500 ms"
    }
    return cmds
  }

def setStatusLed (BigDecimal color, BigDecimal blink) {
    logDebug "setStatusLed($color, $blink)"
    def cmds = []

    if(state.statusled == null) {
        state.statusled=0
        state.blinkval=0
    }

    /* set led color */
    state.statusled=color

    if(state.statusled == 0) {
        // Color is off , put back to NORMAL mode
        cmds << zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 13, size: 1).format()
    }
    else {
        // if LED color is set, put to STATUS mode
        cmds << zwave.configurationV2.configurationSet(configurationValue: [1], parameterNumber: 13, size: 1).format()
        // set the LED to color
        cmds << zwave.configurationV2.configurationSet(configurationValue: [color.intValue()], parameterNumber: 21, size: 1).format()
        // check if LED should be blinking
        def blinkval = state.blinkval
        if(blink) {
            // set blink speed, also enables blink, 1=100ms blink frequency
            cmds << zwave.configurationV2.configurationSet(configurationValue: [5], parameterNumber: 31, size: 1).format()
            state.blinkval = blinkval
        }
        else {
            cmds << zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 31, size: 1).format()
            state.blinkval = blinkval
        }
    }
    delayBetween(cmds, 500)
}

/*
 * Set switch to Normal mode (exit status mode)
 *
 */
def setSwitchModeNormal() {
    logDebug "setSwitchModeNormal()"
    def cmds = []
    cmds << zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 13, size: 1).format()
    delayBetween(cmds, 500)
}

/*
 * Set switch to Status mode (exit normal mode)
 *
 */
def setSwitchModeStatus() {
    logDebug "setSwitchModeStatus()"
    def cmds = []
    cmds << zwave.configurationV2.configurationSet(configurationValue: [1], parameterNumber: 13, size: 1).format()
    delayBetween(cmds, 500)
}

/*
 * Sets LED operation (in normal mode) [LED ON if load is OFF]
 *
 */
def locatorLightOn() {
  logDebug "setLocatorLightOn()"
  def cmds = []
  cmds << zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 3, size: 1).format()
  delayBetween(cmds, 500)
}

/*
 * Sets LED operation (in normal mode) [LED OFF if load is OFF]
 *
 */
def locatorLightOff() {
  logDebug "setLocatorLightOff()"
  def cmds = []
  cmds << zwave.configurationV2.configurationSet(configurationValue: [1], parameterNumber: 3, size: 1).format()
  delayBetween(cmds, 500)
}

/*
 * Set the color of the LEDS for normal operation mode, load light
 */
def setDefaultColor(color) {
    logDebug "setDefaultColor($color)"
    def cmds = []
    cmds << zwave.configurationV2.configurationSet(configurationValue: [color.intValue()], parameterNumber: 14, size: 1).format()
    logTrace "cmds: $cmds"
    delayBetween(cmds, 500)
}

def refresh() {
    logDebug "refresh()"
    configure()
}

def zwaveEvent(hubitat.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    logDebug "zwaveEvent(hubitat.zwave.commands.centralscenev1.CentralSceneNotification cmd)"
    logTrace "cmd: $cmd"
    logDebug("sceneNumber: ${cmd.sceneNumber} keyAttributes: ${cmd.keyAttributes}")
    def result = []

    switch (cmd.sceneNumber) {
        case 1:
        // Up
            switch (cmd.keyAttributes) {
                case 0:
                // Press Once
                    result += createEvent(tapUp1Response("physical"))
                    result += createEvent([name: "switch", value: "on", type: "physical"])
                    break
                case 1:
                    result=createEvent([name: "switch", value: "on", type: "physical"])
                    break
                case 2:
                // Hold
                    result += createEvent(holdUpResponse("physical"))
                    result += createEvent([name: "switch", value: "on", type: "physical"])
                    break
                case 3:
                // 2 Times
                    result=createEvent(tapUp2Response("physical"))
                    break
                case 4:
                // 3 times
                    result=createEvent(tapUp3Response("physical"))
                    break
                case 5:
                // 4 times
                    result=createEvent(tapUp4Response("physical"))
                    break
                case 6:
                // 5 times
                    result=createEvent(tapUp5Response("physical"))
                    break
                default:
                    logDebug ("unexpected up press keyAttribute: $cmd.keyAttributes")
            }
        break

        case 2:
        // Down
            switch (cmd.keyAttributes) {
                case 0:
                // Press Once
                    result += createEvent(tapDown1Response("physical"))
                    result += createEvent([name: "switch", value: "off", type: "physical"])
                    break
                case 1:
                    result=createEvent([name: "switch", value: "off", type: "physical"])
                    break
                case 2:
                // Hold
                    result += createEvent(holdDownResponse("physical"))
                    result += createEvent([name: "switch", value: "off", type: "physical"])
                    break
                case 3:
                // 2 Times
                    result=createEvent(tapDown2Response("physical"))
                    break
                case 4:
                // 3 Times
                    result=createEvent(tapDown3Response("physical"))
                    break
                case 5:
                // 4 Times
                    result=createEvent(tapDown4Response("physical"))
                    break
                case 6:
                // 5 Times
                    result=createEvent(tapDown5Response("physical"))
                    break
                default:
                    logDebug("unexpected down press keyAttribute: $cmd.keyAttributes")
            }
        break

        default:
            // unexpected case
            log.warn("unexpected scene: $cmd.sceneNumber")   }
    return result
}

def tapUp1Response(String buttonType) {
    sendEvent(name: "status", value: "Tap ▲")
    [name: "pushed", value: 7, descriptionText: "$device.displayName Tap-Up-1 (button 7) pressed", isStateChange: true]
}

def tapDown1Response(String buttonType) {
    sendEvent(name: "status", value: "Tap ▼")
    [name: "pushed", value: 8, descriptionText: "$device.displayName Tap-Down-1 (button 8) pressed", isStateChange: true]
}

def tapUp2Response(String buttonType) {
    sendEvent(name: "status", value: "Tap ▲▲")
    [name: "pushed", value: 1, descriptionText: "$device.displayName Tap-Up-2 (button 1) pressed", isStateChange: true]
}

def tapDown2Response(String buttonType) {
    sendEvent(name: "status", value: "Tap ▼▼")
    [name: "pushed", value: 2, descriptionText: "$device.displayName Tap-Down-2 (button 2) pressed", isStateChange: true]
}

def tapUp3Response(String buttonType) {
    sendEvent(name: "status", value: "Tap ▲▲▲")
    [name: "pushed", value: 3, descriptionText: "$device.displayName Tap-Up-3 (button 3) pressed", isStateChange: true]
}

def tapUp4Response(String buttonType) {
    sendEvent(name: "status", value: "Tap ▲▲▲▲")
    [name: "pushed", value: 9, descriptionText: "$device.displayName Tap-Up-4 (button 9) pressed", isStateChange: true]
}

def tapUp5Response(String buttonType) {
    sendEvent(name: "status", value: "Tap ▲▲▲▲▲")
    [name: "pushed", value: 11, descriptionText: "$device.displayName Tap-Up-5 (button 11) pressed", isStateChange: true]
}

def tapDown3Response(String buttonType) {
    sendEvent(name: "status", value: "Tap ▼▼▼")
    [name: "pushed", value: 4, descriptionText: "$device.displayName Tap-Down-3 (button 4) pressed", isStateChange: true]
}

def tapDown4Response(String buttonType) {
    sendEvent(name: "status", value: "Tap ▼▼▼▼")
    [name: "pushed", value: 10, descriptionText: "$device.displayName Tap-Down-3 (button 10) pressed", isStateChange: true]
}

def tapDown5Response(String buttonType) {
    sendEvent(name: "status", value: "Tap ▼▼▼▼▼")
    [name: "pushed", value: 12, descriptionText: "$device.displayName Tap-Down-3 (button 12) pressed", isStateChange: true]
}

def holdUpResponse(String buttonType) {
    sendEvent(name: "status", value: "Hold ▲")
    [name: "pushed", value: 5, descriptionText: "$device.displayName Hold-Up (button 5) pressed", isStateChange: true]
}

def holdDownResponse(String buttonType) {
    sendEvent(name: "status", value: "Hold ▼")
    [name: "pushed", value: 6, descriptionText: "$device.displayName Hold-Down (button 6) pressed", isStateChange: true]
}

def tapUp2() {
sendEvent(tapUp2Response("digital"))
}

def tapDown2() {
sendEvent(tapDown2Response("digital"))
}

def tapUp3() {
sendEvent(tapUp3Response("digital"))
}

def tapDown3() {
sendEvent(tapDown3Response("digital"))
}

def tapUp4() {
sendEvent(tapUp4Response("digital"))
}

def tapDown4() {
sendEvent(tapDown4Response("digital"))
}

def tapUp5() {
sendEvent(tapUp5Response("digital"))
}

def tapDown5() {
sendEvent(tapDown5Response("digital"))
}

def holdUp() {
sendEvent(holdUpResponse("digital"))
}

def holdDown() {
sendEvent(holdDownResponse("digital"))
}

def setFirmwareVersion() {
   def versionInfo = ''
   if (state.manufacturer) {
      versionInfo=state.manufacturer+' '
   }
   if (state.firmwareVersion) {
      versionInfo=versionInfo+"Firmware V"+state.firmwareVersion
   }
   else {
     versionInfo=versionInfo+"Firmware unknown"
   }
   sendEvent(name: "firmwareVersion",  value: versionInfo, isStateChange: true, displayed: false)
}

def configure() {
    logDebug("configure()")
    cleanup()
    sendEvent(name: "numberOfButtons", value: 12, displayed: false)
    def cmds = []
    cmds += setPrefs()
    cmds << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
    cmds << zwave.versionV1.versionGet().format()
    cmds << zwave.switchBinaryV1.switchBinaryGet().format()
    delayBetween(cmds, 500)
}

def setPrefs() {
   logDebug "setPrefs()"
   def cmds = []

    if (logEnable || traceLogEnable) {
        log.warn "Debug logging is on and will be scheduled to turn off automatically in 30 minutes."
        unschedule()
        runIn(1800, logsOff)
    }

    if (color) {
        switch (color) {
            case "White":
                cmds << zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 14, size: 1).format()
                break
            case "Red":
                cmds << zwave.configurationV2.configurationSet(configurationValue: [1], parameterNumber: 14, size: 1).format()
                break
            case "Green":
                cmds << zwave.configurationV2.configurationSet(configurationValue: [2], parameterNumber: 14, size: 1).format()
                break
            case "Blue":
                cmds << zwave.configurationV2.configurationSet(configurationValue: [3], parameterNumber: 14, size: 1).format()
                break
            case "Magenta":
                cmds << zwave.configurationV2.configurationSet(configurationValue: [4], parameterNumber: 14, size: 1).format()
                break
            case "Yellow":
                cmds << zwave.configurationV2.configurationSet(configurationValue: [5], parameterNumber: 14, size: 1).format()
                break
            case "Cyan":
                cmds << zwave.configurationV2.configurationSet(configurationValue: [6], parameterNumber: 14, size: 1).format()
                break
        }
    }

   if (reverseSwitch) {
       cmds << zwave.configurationV2.configurationSet(configurationValue: [1], parameterNumber: 4, size: 1).format()
   }
   else {
      cmds << zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 4, size: 1).format()
   }

   if (locatorlight) {
       cmds << zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 3, size: 1).format()
   }
   else {
      cmds << zwave.configurationV2.configurationSet(configurationValue: [1], parameterNumber: 3, size: 1).format()
   }

   //Enable the following configuration gets to verify configuration in the logs
   //cmds << zwave.configurationV1.configurationGet(parameterNumber: 7).format()
   //cmds << zwave.configurationV1.configurationGet(parameterNumber: 8).format()
   //cmds << zwave.configurationV1.configurationGet(parameterNumber: 9).format()
   //cmds << zwave.configurationV1.configurationGet(parameterNumber: 10).format()

   logTrace "cmds: $cmds"
   return cmds
}

def logsOff() {
    log.info "Turning off debug logging for device ${device.label}"
    device.updateSetting("logEnable", [value: "false", type: "bool"])
    device.updateSetting("traceLogEnable", [value: "false", type: "bool"])
}

def updated() {
    logDebug "updated()"
    def cmds = []
    cmds += setPrefs()
    delayBetween(cmds, 500)
}

def installed() {
    logDebug "installed()"
    cleanup()
}

def cleanup() {
    unschedule()

    logDebug "cleanup()"
    if (state.blinkval != null) {
        state.remove("blinkval")
    }
    if (state.bin != null) {
        state.remove("bin")
    }
    if (state.blinkDuration != null) {
        state.remove("blinkDuration")
    }
    if (state.statusled != null) {
        state.remove(statusled)
    }
}

private logInfo(msg) {
    if (descriptionTextEnable) log.info msg
}

def logDebug(msg) {
    if (logEnable) log.debug msg
}

def logTrace(msg) {
    if (traceLogEnable) log.trace msg
}
