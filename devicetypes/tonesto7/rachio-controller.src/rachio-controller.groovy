/**
 *  Rachio Controller Device Handler
 *
 *  Copyrightę 2018 Anthony Santilli
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
 *	Modified: 5-09-2018
 */

import java.text.SimpleDateFormat

def devVer() { return "1.0.1" }

metadata {
	definition (name: "Rachio Controller", namespace: "tonesto7", author: "Anthony Santilli") {
		capability "Refresh"
		capability "Switch"
		capability "Actuator"
		capability "Valve"
		capability "Sensor"
		capability "Polling"
		capability "Health Check"

		attribute "hardwareModel", "string"
		attribute "hardwareDesc", "string"
		attribute "activeZoneCnt", "number"
		attribute "controllerRunStatus", "string"
		attribute "controllerOn", "string"

		attribute "rainDelay","number"
		attribute "watering", "string"

		attribute "lastWateredDuration", "number"
		attribute "lastWateredDt", "string"
		attribute "lastWateredDesc", "string"

		//current_schedule data
		attribute "scheduleType", "string"
		//attribute "scheduleTypeBtnDesc", "string"
		attribute "curZoneRunStatus", "string"
		attribute "curZoneId", "string"
		attribute "startDate", "number"
		attribute "duration", "number"
		attribute "curZoneName", "string"
		attribute "curZoneNumber", "number"
		attribute "curZoneDuration", "number"
		attribute "curZoneStartDate", "string"
		attribute "curZoneIsCycling", "string"
		attribute "curZoneCycleCount", "number"
		attribute "curZoneWaterTime", "number"
		attribute "totalCycleCount", "number"
		attribute "rainDelayStr", "string"
		attribute "standbyMode", "string"
		attribute "durationNoCycle", "number"

		attribute "lastUpdatedDt", "string"

		command "stopWatering"
		command "setRainDelay", ["number"]

		command "doSetRainDelay"
		command "decreaseRainDelay"
		command "increaseRainDelay"
		command "log"
		command "setZoneWaterTime", ["number"]
		command "runAllZones"
		command "standbyOn"
		command "standbyOff"
		//command "pauseScheduleRun"

		command "open"
		command "close"
		//command "pause"
	}

	tiles (scale: 2){
		multiAttributeTile(name: "valveTile", type: "generic", width: 6, height: 4) {
			tileAttribute("device.watering", key: "PRIMARY_CONTROL" ) {
				attributeState "on", label: 'Watering', action: "close", icon: "st.valves.water.open", backgroundColor: "#00A7E1", nextState: "off"
				attributeState "off", label: 'Off', action: "runAllZones", icon: "st.valves.water.closed", backgroundColor: "#7e7d7d", nextState:"on"
				attributeState "offline", label: 'Offline', action: "refresh", icon: "st.valves.water.closed", backgroundColor: "#FE2E2E"
				attributeState "standby", label: 'Standby Mode', icon: "st.valves.water.closed", backgroundColor: "#FFAE42"
			}
			tileAttribute("device.curZoneRunStatus", key: "SECONDARY_CONTROL") {
				attributeState("default", label:'${currentValue}')
			}
		}
		standardTile("hardwareModel", "device.hardwareModel", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "default", label: '', icon: ""
			state "8ZoneV1", label: '', icon: "https://s3-us-west-2.amazonaws.com/rachio-media/smartthings/8zone_v1.png"
			state "16ZoneV1", label: '', icon: "https://s3-us-west-2.amazonaws.com/rachio-media/smartthings/16zone_v1.png"
			state "8ZoneV2", label: '', icon: "https://s3-us-west-2.amazonaws.com/rachio-media/smartthings/8zone_v2.jpg"
			state "16ZoneV2", label: '', icon: "https://s3-us-west-2.amazonaws.com/rachio-media/smartthings/16zone_v2.jpg"
			state "8ZoneV3", label: '', icon: "https://s3-us-west-2.amazonaws.com/rachio-media/smartthings/8zone_v3.jpg"
			state "16ZoneV3", label: '', icon: "https://s3-us-west-2.amazonaws.com/rachio-media/smartthings/16zone_v3.jpg"
		}
		valueTile("hardwareDesc", "device.hardwareDesc", inactiveLabel: false, width: 4, height: 1, decoration: "flat") {
			state "default", label: 'Model:\n${currentValue}', icon: ""
		}
		valueTile("activeZoneCnt", "device.activeZoneCnt", inactiveLabel: true, width: 4, height: 1, decoration: "flat") {
			state("default", label: 'Active Zones:\n${currentValue}')
		}
		valueTile("controllerOn", "device.controllerOn", inactiveLabel: true, width: 2, height: 1, decoration: "flat") {
			state("default", label: 'Online Status:\n${currentValue}')
		}
		valueTile("controllerRunStatus", "device.controllerRunStatus", inactiveLabel: true, width: 4, height: 2, decoration: "flat") {
			state("default", label: '${currentValue}')
		}
		valueTile("blank", "device.blank", width: 2, height: 1, decoration: "flat") {
			state("default", label: '')
		}
		standardTile("switch", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "off", icon: "st.switch.off"
			state "on", action: "stopWatering", icon: "st.switch.on"
		}
		valueTile("pauseScheduleRun", "device.scheduleTypeBtnDesc", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
			state "default", label: '${currentValue}', action: "pauseScheduleRun"
		}

		// Rain Delay Control
		standardTile("leftButtonControl", "device.rainDelay", inactiveLabel: false, decoration: "flat") {
			state "default", action:"decreaseRainDelay", icon:"st.thermostat.thermostat-left"
		}
		valueTile("rainDelay", "device.rainDelay", width: 2, height: 1, decoration: "flat") {
			state "default", label:'Rain Delay:\n${currentValue} Days'
		}
		standardTile("rightButtonControl", "device.rainDelay", inactiveLabel: false, decoration: "flat") {
			state "default", action:"increaseRainDelay", icon:"st.thermostat.thermostat-right"
		}
		valueTile("applyRainDelay", "device.rainDelayStr", width: 2, height: 1, inactiveLabel: false, decoration: "flat") {
			state "default", label: '${currentValue}', action:'doSetRainDelay'
		}

		//zone Water time control
		valueTile("lastWateredDesc", "device.lastWateredDesc", width: 4, height: 1, decoration: "flat", wordWrap: true) {
			state("default", label: 'Last Watered:\n${currentValue}')
		}
		controlTile("zoneWaterTimeSliderTile", "device.zoneWaterTime", "slider", width: 4, height: 1, range:'(0..60)') {
			state "default", label: 'Manual Zone Time', action:"setZoneWaterTime"
		}
		valueTile("runAllZonesTile", "device.zoneWaterTime", inactiveLabel: false, width: 2 , height: 1, decoration: "flat") {
			state "default", label: 'Run All Zones\n${currentValue} Minutes', action:'runAllZones'
		}
		standardTile("standbyMode", "device.standbyMode", decoration: "flat", wordWrap: true, width: 2, height: 2) {
			state "on", label:'Turn Standby Off', action:"standbyOff", nextState: "false", icon: "http://cdn.device-icons.smartthings.com/sonos/play-icon@2x.png"
			state "off", label:'Turn Standby On', action:"standbyOn", nextState: "true", icon: "http://cdn.device-icons.smartthings.com/sonos/pause-icon@2x.png"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		valueTile("blank21", "device.blank", width: 2, height: 1, decoration: "flat") {
			state("default", label: '')
		}

	}
	main "valveTile"
	details(["valveTile", "hardwareModel", "hardwareDesc", "activeZoneCnt", "curZoneIsCyclingTile", "leftButtonControl", "rainDelay", "rightButtonControl", "applyRainDelay",
			"zoneWaterTimeSliderTile", "runAllZonesTile", "lastUpdatedDt", "standbyMode", "refresh"])
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def initialize() {
	sendEvent(name: "checkInterval", value: (17*60), data: [protocol: "cloud"], displayed: false)
	verifyDataAttr()
}

def verifyDataAttr() {
	if(!device?.getDataValue("manufacturer")) {
		updateDataValue("manufacturer", "Rachio")
	}
	if(!device?.getDataValue("model")) {
		updateDataValue("model", device?.name as String)
	}
}

void installed() {
	initialize()
	state.isInstalled = true
}

void updated() {
	initialize()
}

def ping() {
	log.info "health check ping()..."
	poll()
}

def generateEvent(Map results) {
	if(!state?.swVersion || state?.swVersion != devVer()) {
		initialize()
		state.swVersion = devVer()
	}
	//log.warn "---------------START OF API RESULTS DATA----------------"
	if(results) {
		// log.debug results
		state?.deviceId = device?.deviceNetworkId.toString()
		state?.pauseInStandby = (results?.pauseInStandby == true)
		hardwareModelEvent(results?.data?.model)
		activeZoneCntEvent(results?.data?.zones)
		controllerOnEvent(results?.data?.on)
		def isOnline = results?.status == "ONLINE" ? true : false
		state?.isOnlineStatus = isOnline
		if(!isOnline) {
			markOffLine()
		} else {
			state?.inStandby = results?.standby
			if(isStateChange(device, "standbyMode", results?.standby.toString())) {
				sendEvent(name: 'standbyMode', value: (results?.standby?.toString() == "true" ? "on": "off"), displayed: true, isStateChange: true)
			}
			if(results?.standby == true && results?.pauseInStandby == true) {
				markStandby()
			} else {
				isWateringEvent(results?.schedData?.status, results?.schedData?.zoneId)
			}
		}

		if(!device?.currentState("zoneWaterTime")?.value) {
			setZoneWaterTime(parent?.settings?.defaultZoneTime.toInteger())
		}
		scheduleDataEvent(results?.schedData, results?.data.zones, results?.rainDelay)
		rainDelayValEvent(results?.rainDelay)
		if(isOnline) { lastUpdatedEvent() }
	}
	return "Controller"
}

def getDurationDesc(long secondsCnt) {
	int seconds = secondsCnt %60
	secondsCnt -= seconds
	long minutesCnt = secondsCnt / 60
	long minutes = minutesCnt % 60
	minutesCnt -= minutes
	long hoursCnt = minutesCnt / 60
	return "${minutes} min ${(seconds >= 0 && seconds < 10) ? "0${seconds}" : "${seconds}"} sec"
}

def getDurationMinDesc(long secondsCnt) {
	int seconds = secondsCnt %60
	secondsCnt -= seconds
	long minutesCnt = secondsCnt / 60
	long minutes = minutesCnt % 60
	minutesCnt -= minutes
	long hoursCnt = minutesCnt / 60
	return "${minutes}"
}

def lastUpdatedEvent() {
	def lastDt = formatDt(new Date())
	def lastUpd = device?.currentState("lastUpdatedDt")?.value.toString()
	state?.lastUpdatedDt = lastDt?.toString()
	if(isStateChange(device, "lastUpdatedDt", lastDt.toString())) {
		log.info "${device?.displayName} is (${state?.isOnlineStatus ? "Online and ${state?.inStandby ? "in Standby Mode" : "Active"}" : "OFFLINE"}) - Last Updated: (${lastDt})"
		//log.info "Controller Info Updated: (${lastDt}) | Previous Time: (${lastUpd})"
		sendEvent(name: 'lastUpdatedDt', value: lastDt?.toString(), displayed: false, isStateChange: true)
	}
}

def markOffLine() {
	if(isStateChange(device, "watering", "offline") || isStateChange(device, "curZoneRunStatus", "Device in Offline")) {
		log.debug("UPDATED: Watering is set to (${Offline})")
		sendEvent(name: 'watering', value: "offline", displayed: true, isStateChange: true)
		sendEvent(name: 'valve', value: "closed", displayed: false, isStateChange: true)
		sendEvent(name: 'switch', value: "off", displayed: false, isStateChange: true)
		sendEvent(name: 'curZoneRunStatus', value: "Device is Offline", displayed: false, isStateChange: true)
	}
}

def markStandby() {
	if(isStateChange(device, "watering", "standby") || isStateChange(device, "curZoneRunStatus", "Device in Standby Mode")) {
		log.debug("UPDATED: Watering is set to (${"Standby Mode"})")
		sendEvent(name: 'watering', value: "standby", displayed: true, isStateChange: true)
		sendEvent(name: 'valve', value: "closed", displayed: false, isStateChange: true)
		sendEvent(name: 'switch', value: "off", displayed: false, isStateChange: true)
		sendEvent(name: 'curZoneRunStatus', value: "Device in Standby Mode", displayed: false, isStateChange: true)
	}
}

def isWateringEvent(status, zoneId) {
	//log.trace "isWateringEvent..."
	def curState = device?.currentState("watering")?.value.toString()
	def isOn = (status == "PROCESSING") ? true : false
	def newState = isOn ? "on" : "off"
	def valveState = isOn ? "open" : "close"
	if(isStateChange(device, "watering", newState.toString())) {
		log.debug("UPDATED: Is Watering is set to (${newState}) | Original State: (${curState})")
		sendEvent(name: 'watering', value: newState, displayed: true, isStateChange: true)
		sendEvent(name: 'valve', value: valveState, displayed: false, isStateChange: true)
		sendEvent(name: 'switch', value: newState, displayed: false, isStateChange: true)
		parent?.handleWateringSched(isOn)
	}
}

def hardwareModelEvent(val) {
	def curModel = device?.currentState("hardwareModel")?.value.toString()
	def curDesc = device?.currentState("hardwareDesc")?.value.toString()
	def newModel = null
	def newDesc = null
	switch(val) {
		case "GENERATION1_8ZONE":
			newModel = "8ZoneV1"
			newDesc = "8-Zone (Gen 1)"
			break
		case "GENERATION1_16ZONE":
			newModel = "16ZoneV1"
			newDesc = "16-Zone (Gen 1)"
			break
		case "GENERATION2_8ZONE":
			newModel = "8ZoneV2"
			newDesc = "8-Zone (Gen 2)"
			break
		case "GENERATION2_16ZONE":
			newModel = "16ZoneV2"
			newDesc = "16-Zone (Gen 2)"
			break
		case "GENERATION3_8ZONE":
			newModel = "8ZoneV3"
			newDesc = "8-Zone (Gen 3)"
			break
		case "GENERATION3_16ZONE":
			newModel = "16ZoneV3"
			newDesc = "16-Zone (Gen 3)"
			break
	}
	if(isStateChange(device, "hardwareModel", newModel.toString())) {
		log.debug "UPDATED: Controller Model is (${newModel}) | Original State: (${curModel})"
		sendEvent(name: 'hardwareModel', value: newModel, displayed: true, isStateChange: true)
	}
	if(isStateChange(device, "hardwareDesc", newDesc.toString())) {
		log.debug "UPDATED: Controller Description is (${newDesc}) | Original State: (${curDesc})"
		sendEvent(name: 'hardwareDesc', value: newDesc.toString(), displayed: true, isStateChange: true)
	}
}

def activeZoneCntEvent(zData) {
	def curState = device?.currentValue("activeZoneCnt")?.toString()
	def zoneCnt = 0
	if (zData) {
		zData.each { z ->
			if(z?.enabled.toString() == "true") { zoneCnt = zoneCnt+1 }
		}
		//log.debug "final zoneCnt: $zoneCnt"
	}
	if(isStateChange(device, "activeZoneCnt", zoneCnt.toString())) {
		log.debug "UPDATED: Active Zone Count is (${zoneCnt}) | Original State: (${curState})"
		sendEvent(name: 'activeZoneCnt', value: zoneCnt?.toInteger(), displayed: true, isStateChange: true)
	}
}

def controllerOnEvent(val) {
	def curState = device?.currentState("controllerOn")?.value
	def newState = val?.toString()
	if(isStateChange(device, "controllerOn", newState.toString())) {
		log.debug "UPDATED: Controller On Status is (${newState}) | Original State: (${curState})"
		sendEvent(name: 'controllerOn', value: newState, displayed: true, isStateChange: true)
	}
}

def lastWateredDateEvent(val, dur) {
	def newState = "${epochToDt(val)}"
	def newDesc = "${epochToDt(val)}\nDuration: ${getDurationDesc(dur?.toLong())}"
	def curState = device?.currentState("lastWateredDt")?.value
	if(isStateChange(device, "lastWateredDt", newState.toString())) {
		log.debug "UPDATED: Last Watered Date is (${newState}) | Original State: (${curState})"
		sendEvent(name: 'lastWateredDt', value: newState, displayed: true, isStateChange: true)
		sendEvent(name: 'lastWateredDesc', value: newDesc, displayed: false, isStateChange: true)
	}
}

def rainDelayValEvent(val) {
	def curState = device?.currentState("rainDelay")?.value.toString()
	def newState = val ? val : 0
	if(isStateChange(device, "rainDelay", newState.toString())) {
		log.debug("UPDATED: Rain Delay Value is set to (${newState}) | Original State: (${curState})")
		sendEvent(name:'rainDelay', value: newState, displayed: true)
		setRainDelayString(newState)
	}
}

def setZoneWaterTime(timeVal) {
	def curState = device?.currentState("curZoneWaterTime")?.value.toString()
	def newVal = timeVal ? timeVal.toInteger() : parent?.settings?.defaultZoneTime.toInteger()
	if(isStateChange(device, "curZoneWaterTime", newVal.toString())) {
		log.debug("UPDATED: Manual Zone Water Time is now (${newVal}) | Original State: (${curState})")
		sendEvent(name: 'curZoneWaterTime', value: newVal, displayed: true)
	}
}

def scheduleDataEvent(sData, zData, rainDelay) {
	//log.trace "scheduleDataEvent($data)..."
	def curSchedType = !sData?.type ? "Off" : sData?.type?.toString().capitalize()
	//def curSchedTypeBtnDesc = (!curSchedType || curSchedType in ["off", "manual"]) ? "Pause Disabled" : "Pause Schedule"
	state.curSchedType = curSchedType
	state?.curScheduleId = !sData?.scheduleId ? null : sData?.scheduleId
	state?.curScheduleRuleId = !sData?.scheduleRuleId ? null : sData?.scheduleRuleId
	def zoneData = sData && zData ? getZoneData(zData, sData?.zoneId) : null
	def zoneId = !zoneData ? null : sData?.zoneId
	def zoneName = !zoneData ? null : zoneData?.name
	def zoneNum = !zoneData ? null : zoneData?.zoneNumber

	def zoneStartDate = sData?.zoneStartDate ? sData?.zoneStartDate : null
	def zoneDuration = sData?.zoneDuration ? sData?.zoneDuration : null
	
	def timeDiff = sData?.zoneStartDate ? GetTimeValDiff(sData?.zoneStartDate.toLong()) : 0
	def elapsedDuration = sData?.zoneStartDate ? getDurationMinDesc(Math.round(timeDiff)) : 0
	def wateringDuration = zoneDuration ? getDurationMinDesc(zoneDuration) : 0
	def zoneRunStatus = ((!zoneStartDate && !zoneDuration) || !zoneId ) ? "Status: Idle" : "${zoneName}: (${elapsedDuration} of ${wateringDuration} Minutes)"

	def zoneCycleCount = !sData?.totalCycleCount ? 0 : sData?.totalCycleCount
	def zoneIsCycling =  !sData?.cycling ? false : sData?.cycling
	if(isStateChange(device, "scheduleType", curSchedType?.toString().capitalize())) {
		sendEvent(name: 'scheduleType', value: curSchedType?.toString().capitalize(), displayed: true, isStateChange: true)
	}
	//sendEvent(name: 'scheduleTypeBtnDesc', value: curSchedTypeBtnDesc , displayed: false, isStateChange: true)
	if(!state?.inStandby && isStateChange(device, "curZoneRunStatus", zoneRunStatus?.toString())) {
		sendEvent(name: 'curZoneRunStatus', value: zoneRunStatus?.toString(), displayed: false, isStateChange: true)
	}
	if(isStateChange(device, "curZoneDuration", zoneDuration?.toString())) {
		sendEvent(name: 'curZoneDuration', value: zoneDuration?.toString(), displayed: true, isStateChange: true)
	}
	if(isStateChange(device, "curZoneName", zoneName?.toString())) {
		sendEvent(name: 'curZoneName', value: zoneName?.toString(), displayed: true, isStateChange: true)
	}
	if(isStateChange(device, "curZoneNumber", zoneNum?.toString())) {
		sendEvent(name: 'curZoneNumber', value: zoneNum, displayed: true, isStateChange: true)
	}
	if(isStateChange(device, "curZoneCycleCount", zoneCycleCount?.toString())) {
		sendEvent(name: 'curZoneCycleCount', value: zoneCycleCount, displayed: true, isStateChange: true)
	}
	if(isStateChange(device, "curZoneIsCycling", zoneIsCycling?.toString().capitalize())) {
		sendEvent(name: 'curZoneIsCycling', value: zoneIsCycling?.toString().capitalize(), displayed: true, isStateChange: true)
	}
	if(isStateChange(device, "curZoneStartDate", (zoneStartDate ? epochToDt(zoneStartDate).toString() : "Not Active"))) {
		sendEvent(name: 'curZoneStartDate', value: (zoneStartDate ? epochToDt(zoneStartDate).toString() : "Not Active"), displayed: true, isStateChange: true)
	}
}

def getZoneData(zData, zId) {
	if (zData && zId) {
		def zone
		zData.each { z ->
			if(zId == z?.id) {
				zone = z
			}
		}
		//log.debug "zone: $zone"
		return zone
	}
}

def setRainDelayString( rainDelay) {
	def rainDelayStr = "No Rain Delay";
	if( rainDelay > 0) {
		rainDelayStr = "Rain Delayed";
	}
	sendEvent( name: "rainDelayStr", value: rainDelayStr, isStateChange: true)
}

def doSetRainDelay() {
	def value = device.latestValue('rainDelay')
	log.debug("Set Rain Delay ${value}")
	def res = parent?.setRainDelay(this, value);
	if( !res) {
		markOffLine()
	}
	setRainDelayString(value)
	//parent?.pollChildren()
}

def updateRainDelay(value) {
	sendEvent( name: "rainDelayStr", value: "Set New Rain Delay", isStateChange: true)
	log.debug("Update ${value} ")
	if( value > 7) {
		value = 7;
	} else if ( value < 0) {
		value = 0
	}
	sendEvent(name: 'rainDelay', value: value, displayed: true)
}

def increaseRainDelay() {
	log.debug("Increase Rain Delay");
	def value = device.latestValue('rainDelay')
	updateRainDelay(value + 1)
}

def decreaseRainDelay() {
	log.debug("Decrease Rain Delay");
	def value = device.latestValue('rainDelay')
	updateRainDelay(value - 1)
}

def refresh() {
	//log.trace "refresh..."
	poll()
}

void poll() {
	log.info("Requested Parent Poll...");
	parent?.poll(this)
}

def isCmdOk2Run() {
	//log.trace "isCmdOk2Run..."
	if(state?.pauseInStandby == true && state?.inStandby == true) {
		log.warn "Skipping the request... Because the controller is unable to send commands while it is in standby mode!!!"
		return false
	} else { return true }
}

def runAllZones() {
	log.trace "runAllZones..."
	if(!isCmdOk2Run()) { return }
	def waterTime = device?.latestValue('zoneWaterTime')
	log.debug("Sending Run All Zones for (${waterTime} Minutes)")
	def res = parent?.runAllZones(this, waterTime)
	if (!res) {
		markOffLine()
	}
}

def pauseScheduleRun() {
	log.trace "pauseScheduleRun... NOT AVAILABLE YET!!!"
	if(state?.curSchedType == "automatic") {
		def res = parent?.pauseScheduleRun(this)
		//if(res) { log.info "Successfully Paused Scheduled Run..." }
	}
}

def standbyOn() {
	log.trace "standbyOn..."
	def inStandby = device?.currentState("standbyMode")?.value.toString() == "on" ? true : false
	if (!inStandby) {
		if(parent?.standbyOn(this, state?.deviceId)) {
			sendEvent(name: 'standbyMode', value: "on", displayed: true, isStateChange: true)
		}
	}
	else { log.info "Device is Already in Standby... Ignoring..." }
}

def standbyOff() {
	log.trace "standbyOff..."
	def inStandby = device?.currentState("standbyMode")?.value.toString() == "on" ? true : false
	if (inStandby) {
		if(parent?.standbyOff(this, state?.deviceId)) {
			sendEvent(name: 'standbyMode', value: "off", displayed: true, isStateChange: true)
		}
	}
	else { log.info "Device is Already out of Standby... Ignoring..." }
}

def on() {
	log.trace "on..."
	if(!isCmdOk2Run()) { return }
	def isOn = device?.currentState("switch")?.value.toString() == "on" ? true : false
	if (!isOn) { open() }
	else { log.info "Switch is Already ON... Ignoring..." }
}

def off() {
	log.trace "off..."
	//if(!isCmdOk2Run()) { return }
	def isOff = device?.currentState("switch")?.value.toString() == "off" ? true : false
	if (!isOff) { close() }
	else { log.info "Switch is Already OFF... Ignoring..." }
}

def open() {
	log.trace "open()..."
	log.info "open command is not currently supported by the controller device..."
}

def close() {
	log.trace "close()..."
	//if(!isCmdOk2Run()) { return }
	def isClosed = device?.currentState("valve")?.value.toString() == "closed" ? true : false
	if (!isClosed) {
		def res = parent?.off(this, state?.deviceId)
		if (res) {
			sendEvent(name:'watering', value: "off", displayed: true, isStateChange: true)
			sendEvent(name:'switch', value: "off", displayed: false, isStateChange: true)
			sendEvent(name:'valve', value: "closed", displayed: false, isStateChange: true)
		} else {
			log.trace "close(). marking offline"
			markOffLine();
		}
	}
	else { log.info "Close command Ignored... The Valve is Already Closed" }
}

// To be used directly by smart apps
def stopWatering() {
	log.trace "stopWatering"
	close()
}

def setRainDelay(rainDelay) {
	sendEvent("name":"rainDelay", "value": value)
	def res = parent?.setRainDelay(this, value);
	//if (res) { parent?.pollChildren() }
}

 //This will Print logs from the parent app when added to parent method that the child calls
def log(message, level = "trace") {
	switch (level) {
		case "trace":
			log.trace "PARENT_Log>> " + message
			break
		case "debug":
			log.debug "PARENT_Log>> " + message
			break
		case "warn":
			log.warn "PARENT_Log>> " + message
			break
		case "error":
			log.error "PARENT_Log>> " + message
			break
		default:
			log.error "PARENT_Log>> " + message
			break
	}
	return null // always child interface call with a return value
}

def epochToDt(val) {
	return formatDt(new Date(val))
}

def formatDt(dt) {
	def tf = new SimpleDateFormat("MMM d, yyyy - h:mm:ss a")
	if(location?.timeZone) { tf?.setTimeZone(location?.timeZone) }
	else {
		log.warn "SmartThings TimeZone is not found or is not set... Please Try to open your ST location and Press Save..."
	}
	return tf.format(dt)
}

//Returns time differences is seconds
def GetTimeValDiff(timeVal) {
	try {
		def start = new Date(timeVal).getTime()
		def now = new Date().getTime()
		def diff = (int) (long) (now - start) / 1000
		//log.debug "diff: $diff"
		return diff
	}
	catch (ex) {
		log.error "GetTimeValDiff Exception: ${ex}"
		return 1000
	}
}
