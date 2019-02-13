<template>
    <div id="full_div">
        <div style="height: 20%; overflow: auto; margin-left: 20px; margin-top: 20px;">
            <h3>Simple map</h3>
            <p>First marker is placed at {{ withPopup.lat }}, {{ withPopup.lng }}</p>
            <p> Center is at {{ currentCenter }} and the zoom is: {{ currentZoom }} </p>
            <button @click="showLongText">Toggle long popup</button>
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
                    v-for="marker in markers"
                    :key="marker.id"
                    :lat-lng="convertLatLngToArray(marker)"
                    @click="alert(marker)">
                <l-tooltip :options="{permanent: true, interactive: true}">
                    <p>Device: {{marker.deviceName}}</p>
                    <p>Current Battery Level: {{getBatteryLevelsForDevice(marker)}}%</p>
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
    import { LMap, LTileLayer, LMarker, LPopup, LTooltip } from 'vue2-leaflet';
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
            LTooltip
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
                enableTooltip: true,
                markers: [],
                topBatteryReports: []
            };
        },
        methods: {
            zoomUpdate(zoom) {
                this.currentZoom = zoom;
            },
            centerUpdate(center) {
                this.currentCenter = center;
            },
            showLongText() {
                this.showParagraph = !this.showParagraph;
            },
            innerClick() {
                alert('Click!');
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
            getBatteryLevelsForDevice(marker) {
                for (var i = 0; i < this.topBatteryReports.length; i++) {
                    var currentBatteryReport = this.topBatteryReports[i];
                    var myMarker;
                    if (marker.deviceName == currentBatteryReport.deviceName) {
                        myMarker = currentBatteryReport.batteryLevel;
                    }
                }

                return myMarker;
            }

        },

        mounted() {
            this.getAllDevices();
            this.findMostRecentBatteryLevel();
        }
    }
</script>