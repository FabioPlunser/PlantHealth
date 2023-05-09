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
    let lowerLimit: any = [];
    let upperLimit: any = [];

    for (let data of sensor.values) {
      let label = new Date(data.timestamp).toLocaleTimeString();
      labels.push(label);
      sensorData.push(data.value);
      lowerLimit.push(data.lowerLimit);
      upperLimit.push(data.upperLimit);

      if (data.aboveLimit) {
        pointBackgroundColor.push("rgba(0,0,255,1)");
      } else if (data.belowLimit) {
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
      pointRadius: 7,
      pointHoverRadius: 7,
      data: sensorData,
    };
    datasets.push(sensorValueSet);
    //---------------------------------------------------------------

    //create SensorLimitsSet

    let lowerLimitSet = {
      label: "Above Limit",
      fill: true,
      lineTension: 0,
      backgroundColor: "rgba(0,0,255, 1)",
      borderColor: "rgba(0,0,255, 1)",
      data: lowerLimit,
      pointRadius: 0,
    };
    let upperLimitSet = {
      label: "Below Limit",
      fill: true,
      lineTension: 0,
      backgroundColor: "rgba(255,0,0, 1)",
      borderColor: "rgba(255,0,0, 1)",
      data: upperLimit,
      pointRadius: 0,
    };
    datasets.push(upperLimitSet);
    datasets.push(lowerLimitSet);

    graphData[sensorType] = {
      labels: labels,
      datasets: datasets,
    };
  }

  return graphData;
}
