package at.ac.uibk.plant_health.models.rest_responses;

import at.ac.uibk.plant_health.models.device.SensorStation;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlantListResponse extends RestResponse {
    private List<InnerPlant> plants;

    public PlantListResponse(List<SensorStation> sensorStations) {
        this.plants = sensorStations.stream().map(InnerPlant::new).toList();
    }

    private class InnerPlant {
        private UUID id;
        @JsonProperty("plant-name")
        private String name;
        @JsonProperty("room-name")
        private String roomName;

        public InnerPlant(SensorStation sensorStation) {
            this.id = sensorStation.getDeviceId();
            this.name = sensorStation.getName();
            this.roomName = sensorStation.getAccessPoint().getRoomName();
        }
    }
}
