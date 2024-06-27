<template>

    <v-data-table
        :headers="headers"
        :items="callView"
        :items-per-page="5"
        class="elevation-1"
    ></v-data-table>

</template>

<script>
    const axios = require('axios').default;

    export default {
        name: 'CallViewView',
        props: {
            value: Object,
            editMode: Boolean,
            isNew: Boolean
        },
        data: () => ({
            headers: [
                { text: "id", value: "id" },
                { text: "callId", value: "callId" },
                { text: "callStatus", value: "callStatus" },
                { text: "userName", value: "userName" },
                { text: "driverName", value: "driverName" },
                { text: "taxiNum", value: "taxiNum" },
                { text: "driveStatus", value: "driveStatus" },
            ],
            callView : [],
        }),
          async created() {
            var temp = await axios.get(axios.fixUrl('/callViews'))

            temp.data._embedded.callViews.map(obj => obj.id=obj._links.self.href.split("/")[obj._links.self.href.split("/").length - 1])

            this.callView = temp.data._embedded.callViews;
        },
        methods: {
        }
    }
</script>

