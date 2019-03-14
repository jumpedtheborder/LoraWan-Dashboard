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
            <v-text-field type="text" label="Device ID" v-model="device.deviceID" placeholder="Device ID" :rules="deviceRules"></v-text-field>
            <v-select v-model="device.group" :items="groups" label="Available Groups"></v-select>
          </v-flex>
        </v-layout>
        <div :v-if="!!message" class="red--text">
          <p>{{message}}</p>
        </div>
      </v-form>
    </v-card-text>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn flat @click="clear">Close</v-btn>
      <v-btn color="primary" @click="next">Next</v-btn>
    </v-card-actions>
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
          group: ''
        },
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
      populateAvailableGroupings() {
        axios.get("/rest/groupings").then(response => {
            this.groups = response.data
        }).catch()
      },
    next() {
      if (this.$refs.form.validate()) {
          this.$router.push({name: 'RegisterDeviceLocation', params: {deviceName: this.device.deviceID, deviceGroup: this.device.group}})
      }
    },
    clear() {
      this.$refs.form.reset()
      this.message = ''
    }
  },
    mounted() {
        this.populateAvailableGroupings();
    }
}
</script>
