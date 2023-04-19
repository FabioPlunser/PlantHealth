package at.ac.uibk.plant_health.models.rest_responses;

import at.ac.uibk.plant_health.models.device.AccessPoint;
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
public class AccessPointListResponse extends RestResponse {
    @JsonProperty("access-points")
    private List<InnerAccessPoint> accessPoints;

    public AccessPointListResponse(List<AccessPoint> accessPoints) {
        this.accessPoints = accessPoints.stream().map(InnerAccessPoint::new).toList();
    }

    @Getter
    private class InnerAccessPoint {
        private UUID id;
        @JsonProperty("room-name")
        private String roomName;
        private boolean locked;

        public InnerAccessPoint(AccessPoint accessPoint ) {
            this.id = accessPoint.getDeviceId();
            this.roomName = accessPoint.getRoomName();
            this.locked = !accessPoint.isUnlocked();
        }
    }
}