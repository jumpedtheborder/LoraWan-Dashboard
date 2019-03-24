<template>
    <div id="full_div">
        <div style="height: 20%; overflow: auto; margin-left: 20px; margin-top: 20px;">
            <h3>Map of devices</h3>
            <button @click="toggleHidden">Toggle problem devices only</button>
            <button @click="toggleTooltip">Toggle tooltip display</button>
        </div>
        <l-map
                :zoom="zoom"
                :center="center"
                :options="mapOptions"
                style="height: 80%; position: absolute"
                @update:center="centerUpdate"
                @update:zoom="zoomUpdate"
        >
            <l-tile-layer
                    :url="url"
                    :attribution="attribution"
            />
            <l-marker
                    v-if="!hidden"
                    v-for="marker in markers"
                    :key="marker.id"
                    :lat-lng="convertLatLngToArray(marker)"
                    @click="routeToCandidateConsists(marker.id)">
                <l-tooltip v-if="enableTooltip" :options="{permanent: enableTooltip}">
                    <p>Device: {{marker.deviceName}}</p>
                    <p>Current Battery Level: {{getBatteryLevelsForDevice(marker)}}%</p>
                </l-tooltip>
            </l-marker>
            <l-marker
                v-if="hidden"
                v-for="problem in problemDevices"
                :key="problem.id"
                :lat-lng="convertLatLngToArray(problem)"
                @click="routeToCandidateConsists(problem.id)">
                <l-icon icon-url="/images/Red_Icon.png" />
                <l-tooltip v-if="enableTooltip" :options="{permanent: true}">
                    <p>Device: {{problem.deviceName}}</p>
                    <p>Current Battery Level: {{getBatteryLevelsForDevice(problem)}}%</p>
                </l-tooltip>
            </l-marker>
            <l-tile-layer
                    :url="url"
                    :attribution="attribution"
            />
        </l-map>
    </div>
</template>

<script>
    import { LMap, LTileLayer, LMarker, LPopup, LTooltip, LIcon } from 'vue2-leaflet';
    import { L } from 'vue2-leaflet';
    import axios from 'axios';
    delete L.Icon.Default.prototype._getIconUrl;



    L.Icon.Default.mergeOptions({
        iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
        iconUrl: require('leaflet/dist/images/marker-icon.png'),
        shadowUrl: require('leaflet/dist/images/marker-shadow.png')
    });

    export default {
        name: 'Example',
        components: {
            LMap,
            LTileLayer,
            LMarker,
            LPopup,
            LTooltip,
            LIcon
        },
        data() {
            return {
                zoom: 7,
                center: L.latLng(51.5074, -0.1278),
                url: 'http://{s}.tile.osm.org/{z}/{x}/{y}.png',
                attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors',
                withPopup: L.latLng(47.413220, -1.219482),
                withTooltip: L.latLng(47.414220, -1.250482),
                currentZoom: 7,
                currentCenter: L.latLng(51.5074, -0.1278),
                showParagraph: false,
                mapOptions: {
                    zoomSnap: 0.5
                },
                problemDevices: [],
                hidden: false,
                enableTooltip: true,
                markers: [],
                topBatteryReports: [],
                stuff: {"applicationID":"1","applicationName":"test-app","deviceName":"com2","devEUI":"c8d63e89945d3b07","rxInfo":[{"gatewayID":"66efb7b9945d3b07","name":"lopy4","time":"2019-02-13T18:15:26.279285Z","rssi":-50,"loRaSNR":7,"location":{"latitude":0,"longitude":0,"altitude":0}}],"txInfo":{"frequency":868100000,"dr":5},"adr":false,"fCnt":7,"fPort":2,"data":"AAV53XdJ"},
                stuff2: {"applicationID":"1","applicationName":"test-app","deviceName":"com2","devEUI":"0de5b209945d3b07","rxInfo":[{"gatewayID":"66efb7b9945d3b07","name":"lopy4","time":"2019-02-13T18:15:27.279285Z","rssi":-50,"loRaSNR":7,"location":{"latitude":0,"longitude":0,"altitude":0}}],"txInfo":{"frequency":868100000,"dr":5},"adr":false,"fCnt":7,"fPort":2,"data":"AAV53XdJ"},
                stuff3: {"applicationID":"1","applicationName":"test-app","deviceName":"com2","devEUI":"testDevice","rxInfo":[{"gatewayID":"66efb7b9945d3b07","name":"lopy4","time":"2019-02-13T18:15:25.279285Z","rssi":-50,"loRaSNR":7,"location":{"latitude":0,"longitude":0,"altitude":0}}],"txInfo":{"frequency":868100000,"dr":5},"adr":false,"fCnt":7,"fPort":2,"data":"AAV53XdJ"}
            };
        },
        methods: {
            zoomUpdate(zoom) {
                this.currentZoom = zoom;
            },
            centerUpdate(center) {
                this.currentCenter = center;
            },
            toggleHidden() {
              this.hidden = !this.hidden
            },
            toggleTooltip() {
              this.enableTooltip = !this.enableTooltip
            },
            getAllDevices() {
                axios.get('/rest/devices').then((response) => {
                    this.markers = response.data
                }).catch(err => {
                })
            },
            convertLatLngToArray(marker) {
                var array = {lng: marker.longitude, lat: marker.latitude}

                return array;
            },
            findMostRecentBatteryLevel() {
                axios.get('/rest/topBatteryReports').then((response) => {
                    this.topBatteryReports = response.data
                }).catch(err => {

                })
            },
            findProblemDevices() {
                axios.get('/rest/problemDevices').then((response) => {
                    this.problemDevices = response.data
                }).catch()
            },
            getBatteryLevelsForDevice(marker) {
                for (var i = 0; i < this.topBatteryReports.length; i++) {
                    var currentBatteryReport = this.topBatteryReports[i];
                    var myMarker;
                    if (marker.deviceName == currentBatteryReport.deviceName) {
                        myMarker = currentBatteryReport.batteryLevel;
                    }
                }
                return myMarker;
            },
            routeToCandidateConsists(id) {
                this.$router.push(`/device/${id}/candidateConsists`)
            },
            testWebhooks1() {
                axios.post("/webhook", {
                    webhookJson: this.stuff
                })
            },
            testWebhooks2() {
                axios.post("/webhook", {
                    webhookJson: this.stuff2
                })
            },
            testWebhooks3() {
                axios.post("/webhook", {
                    webhookJson: this.stuff3
                })
            }
        },

        mounted() {
            this.getAllDevices();
            this.findMostRecentBatteryLevel();
            this.findProblemDevices();
            this.testWebhooks1();
            this.testWebhooks2();
            this.testWebhooks3();
        }
    }
</script>