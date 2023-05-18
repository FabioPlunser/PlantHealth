export function createGraphData(data: any) {
  let graphData: any = {};
  for (let sensor of data) {
    let sensorType = sensor.sensorType;
    let sensorUnit = sensor.sensorUnit;
    let labels = [];
    let datasets = [];

    // create SensorDataSet
    //---------------------------------------------------------------
    let sensorData: any = [];
    let pointBackgroundColor: any = [];

    for (let data of sensor.values) {
      // let date = {
      //   x: new Date(data.timeStamp).toLocaleString("de-DE"),
      //   y: data.value,
      // }
      sensorData.push(data.value);

      if (data.aboveLimit || data.belowLimit) {
        pointBackgroundColor.push("rgba(255,0,0,1)");
      } else {
        pointBackgroundColor.push("rgba(75,192,192,1)");
      }
    }

    let sensorValueSet = {
      label: sensorType,
      fill: true,
      lineTension: 0.5,
      backgroundColor: "rgba(75,192,192,0.5)",
      borderColor: "rgba(75,192,192,1)",
      pointBackgroundColor: pointBackgroundColor,
      pointRadius: 4,
      pointHoverRadius: 4,
      xAxisID: "x",
      yAxisID: "y",
      data: sensorData,
    };
    datasets.push(sensorValueSet);
    let limits = {
      label: "Outside Limits",
      backgroundColor: "rgba(255,0,0,1)",
    };
    datasets.push(limits);
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    graphData[sensorType] = {
      labels: sensor.values.map((value: any) =>
        new Date(value.timeStamp).toLocaleString("de-DE")
      ),
      datasets: datasets,
    };
  }

  return graphData;
}
