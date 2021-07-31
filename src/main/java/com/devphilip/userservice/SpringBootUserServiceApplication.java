package com.devphilip.userservice;

import com.devphilip.userservice.entities.ERole;
import com.devphilip.userservice.entities.Role;
import com.devphilip.userservice.entities.User;
import com.devphilip.userservice.repositories.RoleRepository;
import com.devphilip.userservice.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class SpringBootUserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootUserServiceApplication.class, args);
	}

	@Bean
	public RedisCacheConfiguration cacheConfiguration() {
		return RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(Duration.ofMinutes(60))
				.disableCachingNullValues()
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
	}

	@Bean
	public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
		return (builder) -> builder
				.withCacheConfiguration("userCache",
						RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5)));
	}

	@Bean
	public CommandLineRunner runner(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder encoder) {
		return args -> {
			roleRepository.save(new Role(ERole.ROLE_USER));
			roleRepository.save(new Role(ERole.ROLE_MODERATOR));
			roleRepository.save(new Role(ERole.ROLE_ADMIN));

			Set<Role> roles = new HashSet<>();
			Role userRole = roleRepository.findByName(ERole.ROLE_USER).get();
			Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR).get();
			Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN).get();
			roles.add(userRole);
			roles.add(modRole);
			roles.add(adminRole);

			User adminUser = new User("Philip", "Akpeki", "philipakpeki@gmail.com", encoder.encode("passme"));
			adminUser.setRoles(roles);
			adminUser.setActive(true);
			userRepository.save(adminUser);

			roles.remove(modRole);
			roles.remove(adminRole);
			for (int i = 0; i < 10; i++) {
				User user = new User("user_" + i + "_fn", "user_" + i + "_ln", "user_" + i + "@mail.com", encoder.encode("passme"));
				user.setRoles(roles);
				user.setActive(true);
				userRepository.save(user);
			}

		};
	}

}
