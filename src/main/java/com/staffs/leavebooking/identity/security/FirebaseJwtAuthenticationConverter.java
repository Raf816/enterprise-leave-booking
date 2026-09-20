package com.staffs.leavebooking.identity.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Component
public class FirebaseJwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String roleClaim = jwt.getClaimAsString("role");

        String authority = (roleClaim != null && !roleClaim.isBlank())
                ? Role.PREFIX + roleClaim.toUpperCase()
                : Role.STAFF.getAuthority();

        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(authority)
        );

        return new JwtAuthenticationToken(
                jwt,
                authorities,
                Objects.requireNonNull(jwt.getSubject())
        );
    }
}
