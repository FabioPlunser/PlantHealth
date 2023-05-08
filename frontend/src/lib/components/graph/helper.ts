export function createGraphData(data: any) {
  let graphData: any = {};

  for (let sensor of data) {
    let sensorType = sensor.sensorType;
    let sensorUnit = sensor.sensorUnit;
    let labels = [];
    let datasets = [];

    let sensorData: any = [];
    for (let data of sensor.values) {
      let label = new Date(data.timestamp).toLocaleTimeString();
      labels.push(label);
      sensorData.push(data.value);
    }
    let dataset = {
      label: sensorType,
      fill: true,
      lineTension: 0.5,
      backgroundColor: "rgba(75,192,192,0.4)",
      borderColor: "rgba(75,192,192,1)",
      data: sensorData,
    };
    datasets.push(dataset);

    graphData[sensorType] = {
      labels: labels,
      datasets: datasets,
    };
  }

  return graphData;
}
