export let darkOptions = {
  dark: true,
  maintainAspectRatio: false,
  devicePixelRatio: 4,
  responsive: true,
  plugins: {
    responsive: true,
    legend: {
      title: {
        display: false,
      },
      display: false,
    },
  },
  scales: {
    x: {
      ticks: {
        maxRotation: 90,
        minRotation: 15,
        color: "white",
      },
      grid: {
        color: "white",
      },
    },
    y: {
      ticks: {
        color: "white",
      },
      grid: {
        color: "white",
      },
    },
  },
};

export let lightOptions = {
  light: true,
  maintainAspectRatio: false,
  devicePixelRatio: 4,
  responsive: true,
  plugins: {
    responsive: true,
    legend: {
      title: {
        display: false,
      },
      display: false,
    },
  },
  scales: {
    x: {
      ticks: {
        maxRotation: 90,
        minRotation: 15,
        color: "#abaaa9",
      },
      grid: {
        color: "#abaaa9",
      },
    },
    y: {
      ticks: {
        color: "#abaaa9",
      },
      grid: {
        color: "#abaaa9",
      },
    },
  },
};
