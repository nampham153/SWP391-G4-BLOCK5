package com.example.swp.security;

import com.example.swp.repository.CustomerRepository;
import com.example.swp.repository.ManageRepository;
import com.example.swp.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
public class MyUserDetailService implements UserDetailsService {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private ManageRepository managerRepository;



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return customerRepository.findByEmail(username)
                .map(MyUserDetail::new)
                .or(() -> staffRepository.findByEmail(username).map(MyUserDetail::new))
                .or(() -> managerRepository.findByEmail(username).map(MyUserDetail::new))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }


//

}
