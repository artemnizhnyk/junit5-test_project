package com.artemnizhnyk.junit.paramresolver;

import com.artemnizhnyk.junit.dao.UserDao;
import com.artemnizhnyk.junit.service.UserService;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import static org.junit.jupiter.api.extension.ExtensionContext.*;

public class UserServiceParamResolver implements ParameterResolver {
    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == UserService.class;
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) throws ParameterResolutionException {
        Store store = extensionContext.getStore(Namespace.create(UserService.class));
        return store.getOrComputeIfAbsent(UserService.class, it -> new UserService(new UserDao()));
    }
}
