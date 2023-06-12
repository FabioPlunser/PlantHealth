export function createGraphData(data: Responses.InnerSensors[]) {
  let graphData: any = {};
  for (let sensor of data) {
    let sensorType = sensor.sensorType;
    let sensorUnit = sensor.sensorUnit;
    let labels: any = [];
    let datasets: any = [];

    let sensorLimits = sensor.sensorLimits || [];

    const timeStamps = [
      ...sensor.values.map((data) => new Date(data.timeStamp)),
      // ...sensor.sensorLimits.map(data => new Date(data.timeStamp)),
    ];

    timeStamps.sort((a, b) => a.getTime() - b.getTime());

    // Remove duplicate timestamps (if any)
    const uniqueTimestamps = Array.from(new Set(timeStamps));

    // Create the labels array with the timestamps
    labels = uniqueTimestamps;

    // create SensorDataSet
    //---------------------------------------------------------------
    let sensorData: any = [];
    let pointBackgroundColor: any = [];
    for (let data of sensor.values) {
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
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    if (sensorLimits.length > 0) {
      let upperLimitData: any = [];
      let lowerLimitData: any = [];
      labels.map((label) => {
        sensorLimits.map((limit) => {
          if (label.getTime() === new Date(limit.timeStamp).getTime()) {
            upperLimitData.push(limit.upperLimit);
            lowerLimitData.push(limit.lowerLimit);
          }
        });
        // upperLimitData.push(sensorLimits[sensorLimits.length-1].upperLimit);
        // lowerLimitData.push(sensorLimits[sensorLimits.length-1].lowerLimit);
      });
      // let upperLimitData = sensorLimits.map(data => {

      //   const index = labels.findIndex((label) => label.getTime() === new Date(data.timeStamp).getTime());
      //   return index !== -1 ? data.upperLimit : 0;
      // })
      console.log(upperLimitData);
      let upperLimit = {
        label: "UpperLimit",
        fill: false,
        pointRadius: 4,
        backgroundColor: "rgba(255,0,0,1)",
        borderColor: "rgba(255,0,0,1)",
        data: upperLimitData,
      };
      datasets.push(upperLimit);

      // let lowerLimitData = sensorLimits.map(data => {
      //   const index = labels.findIndex(label => label.getTime() === new Date(data.timeStamp).getTime());
      //   return index !== -1 ? data.lowerLimit : 0;
      // });
      let lowerLimit = {
        label: "UpperLimit",
        fill: false,
        pointRadius: 4,
        backgroundColor: "rgba(0,0,255,1)",
        borderColor: "rgba(0,0,255,1)",
        data: lowerLimitData,
      };
      datasets.push(lowerLimit);
    }
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    let outsideLimits = {
      label: "Outside limits",
      backgroundColor: "rgba(255,0,0,1)",
    };
    datasets.push(outsideLimits);
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    graphData[sensorType] = {
      // labels: sensor.values.map((value: any) =>
      //   new Date(value.timeStamp).toLocaleString("de-DE")
      // ),
      labels: labels.map((label) => label.toLocaleString("de-DE")),
      datasets: datasets,
    };
  }
  console.log(graphData);
  return graphData;
}
