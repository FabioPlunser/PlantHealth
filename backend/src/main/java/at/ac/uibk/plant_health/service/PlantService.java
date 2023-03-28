package at.ac.uibk.plant_health.service;

import at.ac.uibk.plant_health.models.plant.Plant;
import at.ac.uibk.plant_health.models.plant.PlantPicture;
import at.ac.uibk.plant_health.models.plant.SensorLimits;
import at.ac.uibk.plant_health.repositories.PlantPersonReferenceRepository;
import at.ac.uibk.plant_health.repositories.PlantPictureRepository;
import at.ac.uibk.plant_health.repositories.PlantRepository;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
public class PlantService {

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private PlantPictureRepository plantPictureRepository;

    @Autowired
    private PlantPersonReferenceRepository plantPersonReferenceRepository;

    public List<String> getPlantPictures(Plant plant) {
        // TODO
        return List.of();
    }

    public boolean uploadPlantPicture(UUID plantId, String picture) {
        // TODO
        return false;
    }

    public List<Plant> findAllPlants() {
        // TODO
        return List.of();
    }

    public boolean setSensorLimits(List<SensorLimits> sensorLimits) {
        // TODO
        return false;
    }

    public boolean setTransferInterval(int transferInterval) {
        // TODO
        return false;
    }

    public boolean deletePicture(PlantPicture plantPicture) {
        // TODO
        return false;
    }

    public boolean createQrCode(UUID qrCodeId) {
        // TODO
        return false;
    }
}
