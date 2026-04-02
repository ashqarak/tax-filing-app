package com.taxfiling.service;

import com.taxfiling.model.ClientProfile;
import com.taxfiling.model.User;
import com.taxfiling.repository.ClientProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientProfileService {

    private final ClientProfileRepository clientProfileRepository;

    public ClientProfile getOrCreate(User user) {
        return clientProfileRepository.findByUser(user)
                .orElseGet(() -> {
                    ClientProfile profile = new ClientProfile();
                    profile.setUser(user);
                    return clientProfileRepository.save(profile);
                });
    }

    public ClientProfile save(User user, ClientProfile incoming) {
        ClientProfile profile = clientProfileRepository.findByUser(user)
                .orElse(new ClientProfile());
        profile.setUser(user);
        profile.setPan(incoming.getPan());
        profile.setAadhaar(incoming.getAadhaar());
        profile.setDateOfBirth(incoming.getDateOfBirth());
        profile.setMobile(incoming.getMobile());
        profile.setAddress(incoming.getAddress());
        profile.setCity(incoming.getCity());
        profile.setState(incoming.getState());
        profile.setPincode(incoming.getPincode());
        profile.setResidentialStatus(incoming.getResidentialStatus());
        profile.setAgeCategory(incoming.getAgeCategory());
        return clientProfileRepository.save(profile);
    }
}
