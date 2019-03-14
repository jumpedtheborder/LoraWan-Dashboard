<template>
    <v-card>
        <v-card-title>
            <h3 class="headline mb-0">Create a new device</h3>
        </v-card-title>
        <v-card-text>
            <div :v-if="!!message" class="red--text">
                <p>{{message}}</p>
            </div>
            <v-form v-model="valid" ref="form">
                <v-layout row wrap>
                    <v-flex xs12 md6>
                        <p>Credentials:</p>
                        <p>The device name you have entered is: {{deviceName}}</p>
                        <p>The device group you have entered is: {{deviceGroup}}</p>
                        <p>These will be automatically updated upon positioning the device on the below map</p>
                        <v-text-field type="number" label="Latitude of device" v-model.number="markers.position.lat" placeholder="Latitude" :rules="latitudeRules" readonly></v-text-field>
                        <v-text-field type="number" label="Longitude of device" v-model.number="markers.position.lng" placeholder="Longitude" :rules="longitudeRules" readonly></v-text-field>
                        <v-text-field type="text" label="Device region" v-model="device.region" placeholder="Region" :rules="regionRules"></v-text-field>
                        <p>The map below shows some red markers corresponding to the existing devices in this group. Once positioning the blue marker, please give the order in which the marker exists from left to right, up to down. For example, if there are two red markers followed by the blue pin dropped, please enter 3 below.</p>
                        <v-text-field type="number" label="Order value from left to right" v-model="device.order" placeholder="Order"></v-text-field>
                    </v-flex>
                </v-layout>
            </v-form>
        </v-card-text>
        <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn flat @click="clear">Close</v-btn>
            <v-btn color="primary" @click="submit">Submit</v-btn>
        </v-card-actions>
        <div>
            <l-map
                    :zoom.sync="zoom"
                    :options="mapOptions"
                    :center="center"
                    :bounds="bounds"
                    :min-zoom="minZoom"
                    :max-zoom="maxZoom"
                    style="height: 45%; position: absolute;">
                <l-control-layers
                        :position="layersPosition"
                        :collapsed="false"
                        :sort-layers="true"
                />
                <l-tile-layer
                        v-for="tileProvider in tileProviders"
                        :key="tileProvider.name"
                        :name="tileProvider.name"
                        :visible="tileProvider.visible"
                        :url="tileProvider.url"
                        :attribution="tileProvider.attribution"
                        :token="token"
                        layer-type="base"/>
                <l-control-zoom :position="zoomPosition" />
                <l-control-attribution
                        :position="attributionPosition"
                        :prefix="attributionPrefix" />
                <l-control-scale :imperial="imperial" />
                <l-marker
                        :key="markers.id"
                        :visible="markers.visible"
                        :draggable="markers.draggable"
                        :lat-lng.sync="markers.position"
                        :icon="markers.icon"
                        @click="alert(markers)"
                        @dragend="updateDeviceRegion(markers.position)">
                    <l-popup :content="markers.tooltip" />
                    <l-tooltip :content="markers.tooltip" />
                </l-marker>
                <l-marker
                        v-for="marker in groupMarkers"
                        :key="marker.id"
                        :lat-lng="convertLatLngToArray(marker)">
                        <l-icon icon-url="/images/Red_Icon.png" />
                  <l-tooltip :options="{permanent: true}">
                    <p>Device: {{marker.deviceName}}</p>
                  </l-tooltip>
                </l-marker>
            </l-map>
        </div>
    </v-card>

</template>

<script>
    import axios from 'axios'
    import { LMap, LTileLayer, LMarker, LLayerGroup, LTooltip, LPopup, LControlZoom, LControlAttribution, LControlScale, LControlLayers, LGeoJson, LIcon } from 'vue2-leaflet';
    import myJSON from '../assets/GBR_adm2.json';


    const tileProviders = [
        {
            name: 'OpenStreetMap',
            visible: true,
            attribution: '&copy; <a target="_blank" href="http://osm.org/copyright">OpenStreetMap</a> contributors',
            url: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'
        }
    ];

    export default {
        name: 'Example',
        components: {
            LMap,
            LTileLayer,
            LMarker,
            LLayerGroup,
            LTooltip,
            LPopup,
            LControlZoom,
            LControlAttribution,
            LControlScale,
            LControlLayers,
            LGeoJson,
            LIcon
        },
        data() {
            return {
                valid: false,
                center: [51.505, -0.09],
                opacity: 0.6,
                token: 'your token if using mapbox',
                mapOptions: { zoomControl: false, attributionControl: false, zoomSnap: true },
                zoom: 13,
                minZoom: 1,
                maxZoom: 20,
                zoomPosition: 'topleft',
                attributionPosition: 'bottomright',
                layersPosition: 'topright',
                attributionPrefix: 'Vue2Leaflet',
                imperial: false,
                Positions: ['topleft', 'topright', 'bottomleft', 'bottomright'],
                tileProviders: tileProviders,
                bounds: L.latLngBounds({ 'lat': 51.476483373501964, 'lng': -0.14668464136775586 }, { 'lat': 51.52948330894063, 'lng': -0.019140238291583955 }),
                message: '',
                groups: [],
                markers: {
                    id: 'm1',
                    position: {lat: 51.505, lng: -0.09},
                    tooltip: 'Device',
                    draggable: true,
                    visible: true
                },
                device: {
                    deviceID: '',
                    latitude: '',
                    longitude: '',
                    region: '',
                    group: '',
                    order: ''
                },
                groupMarkers: [],
                enableTooltip: true,
                geojson: myJSON,
                deviceRules: [
                    v => !!v || 'Device ID is required',
                    v => v && v.length <= 200 || 'Device ID must be less than 200 characters'
                ],
                latitudeRules: [
                    v => !!v || 'Latitude is required',
                    v => v && v <= 85 && v >= -85 || 'Latitude must be less than 100 characters'
                ],
                longitudeRules: [
                    v => !!v || 'Longitude is required',
                    v => v && v <= 180 && v >= -180 || 'Longitude must be less than 100 characters'
                ],
                regionRules: [
                    v => !!v || 'Region is required',
                    v => v && v.length <= 200 || 'Region must be less than 200 characters'
                ]
            }
        },
        props: {
            deviceName: {
                type: String,
                required: true
            },
            deviceGroup: {
                type: String,
                required: true
            }
        },
        computed: {
            options() {
                return {
                    onEachFeature: this.onEachFeatureFunction
                };
            },
            styleFunction() {
                const fillColor = this.fillColor; // important! need touch fillColor in computed for re-calculate when change fillColor
                return () => {
                    return {
                        weight: 2,
                        color: '#ffffff',
                        opacity: 0.8,
                    };
                };
            },
            onEachFeatureFunction() {
                if (!this.enableTooltip) {
                    return () => {
                    };
                }
                return (feature, layer) => {
                    layer.bindTooltip('<div>code:' + feature.properties.NAME_2 + '</div>', {
                        permanent: false,
                        sticky: true
                    });
                };
            }
        },
        methods: {
            insidePolygon(point, vs) {
                // ray-casting algorithm based on
                // http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html

                var x = point[0], y = point[1];

                var inside = false;
                for (var i = 0, j = vs.length - 1; i < vs.length; j = i++) {
                    var xi = vs[i][0], yi = vs[i][1];
                    var xj = vs[j][0], yj = vs[j][1];

                    var intersect = ((yi > y) != (yj > y))
                        && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
                    if (intersect) inside = !inside;
                }

                return inside;
            },
            insideMultiPolygon(point, vs) {
                // ray-casting algorithm based on
                // http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html

                var x = point[0], y = point[1];

                var inside = false;

                for(var a = 0; a < vs.length; a++) {
                    var test = vs[a][0];
                    for (var i = 0, j = test.length - 1; i < test.length; j = i++) {
                        var xi = test[i][0], yi = test[i][1];
                        var xj = test[j][0], yj = test[j][1];

                        var intersect = ((yi > y) != (yj > y))
                            && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
                        if (intersect) inside = !inside;
                    }
                }

                return inside;
            },
            updateDeviceRegion(lnglat) {

                for (var i = 0; i < this.geojson.features.length; i++) {
                    var currentGeoJson = this.geojson.features[i];
                    var currentLngLat = [lnglat.lng, lnglat.lat];
                    if (currentGeoJson.geometry.type.toString() == "Polygon") {
                        var isTrue = this.insidePolygon(currentLngLat, currentGeoJson.geometry.coordinates[0]);
                        if(isTrue == true) {
                            this.device.region = currentGeoJson.properties.NAME_2;
                        }
                    }
                    else {
                        var isTrue = this.insideMultiPolygon(currentLngLat, currentGeoJson.geometry.coordinates);
                        if(isTrue == true) {
                            this.device.region = currentGeoJson.properties.NAME_2;
                        }
                    }
                }
            },
            populateGroupDevices() {
                axios.get(`/rest/groupDevices/${this.deviceGroup}`).then(response => {
                    this.groupMarkers = response.data
                }).catch()
            },
            convertLatLngToArray(marker) {
                var array = {lng: marker.longitude, lat: marker.latitude}

                return array;
            },
            submit() {
                if (this.$refs.form.validate()) {
                    axios.post("/rest/device", {
                        deviceName: this.deviceName,
                        latitude: this.markers.position.lat,
                        longitude: this.markers.position.lng,
                        region: this.device.region,
                        group: this.deviceGroup,
                        groupOrder: this.device.order
                    }).catch(err => {
                        this.message = err.response.data && err.response.data.message ? 'Error: ' + err.response.data.message : err.message
                    }).finally(() => {
                        this.$router.push('/map')
                    })
                }
            },
            clear() {
                this.$refs.form.reset()
                this.message = ''
            }
        },
        mounted() {
            this.populateGroupDevices();
        }
    }
</script>
