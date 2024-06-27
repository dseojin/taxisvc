<template>

    <v-card outlined>
        <v-card-title>
            CallView # {{$route.params.id }}
        </v-card-title>

        <v-card-text>
            <div>
                <Number label="CallId" v-model="item.callId" :editMode="editMode" @change="change" />
            </div>
            <div>
                <String label="CallStatus" v-model="item.callStatus" :editMode="editMode" @change="change" />
            </div>
            <div>
                <String label="UserName" v-model="item.userName" :editMode="editMode" @change="change" />
            </div>
            <div>
                <String label="DriverName" v-model="item.driverName" :editMode="editMode" @change="change" />
            </div>
            <div>
                <String label="TaxiNum" v-model="item.taxiNum" :editMode="editMode" @change="change" />
            </div>
            <div>
                <String label="DriveStatus" v-model="item.driveStatus" :editMode="editMode" @change="change" />
            </div>
        </v-card-text>
    </v-card>

</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'CallViewViewDetail',
    props: {
      value: Object,
    },
    data: () => ({
        item : [],
    }),
    async created() {
      var params = this.$route.params;
      var temp = await axios.get(axios.fixUrl('/callViews/' + params.id))

      this.item = temp.data;

    },
    methods: {
    }
  }
</script>

