package com.bonitasoft.challenge.service.impl;

import java.util.*;

import com.bonitasoft.challenge.dao.CommentDao;
import com.bonitasoft.challenge.dao.RecipeDao;
import com.bonitasoft.challenge.dao.UserDao;
import com.bonitasoft.challenge.model.Comment;
import com.bonitasoft.challenge.model.Recipe;
import com.bonitasoft.challenge.model.Role;
import com.bonitasoft.challenge.model.User;
import com.bonitasoft.challenge.dto.UserDto;
import com.bonitasoft.challenge.service.RoleService;
import com.bonitasoft.challenge.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User Service interface implementation to get database data.
 * @author: Edgar Salazar
 * @version: 08/15/2021
 */
@Service(value = "userService")
public class UserServiceImpl implements UserDetailsService, UserService {

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private CommentDao commentDao;

    @Autowired
    private RecipeDao recipeDao;

    @Autowired
    private BCryptPasswordEncoder bcryptEncoder;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userDao.findByUsername(username);
        if(user == null){
            throw new UsernameNotFoundException("Invalid username or password.");
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), getAuthority(user));
    }

    private Set<SimpleGrantedAuthority> getAuthority(User user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        });
        return authorities;
    }

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        userDao.findAll().iterator().forEachRemaining(list::add);
        return list;
    }

    @Override
    public User findOne(String username) {
        return userDao.findByUsername(username);
    }

    @Transactional
    @Override
    public User save(UserDto user) {
        User nUser = user.getUserFromDto();
        nUser.setPassword(bcryptEncoder.encode(user.getPassword()));

        Role role = roleService.findByName(user.getRole());
        Set<Role> roleSet = new HashSet<>();
        roleSet.add(role);

        nUser.setRoles(roleSet);
        return userDao.save(nUser);
    }

    @Transactional
    public String delete(Long id){
        Optional<User> nUser = userDao.findById(id);
        List<Comment> del = commentDao.removeByUserId(id);
        List<Recipe> del2 = recipeDao.deleteByUserId(id);
        userDao.delete(nUser.get());
        return "Deleted";
    }

}
