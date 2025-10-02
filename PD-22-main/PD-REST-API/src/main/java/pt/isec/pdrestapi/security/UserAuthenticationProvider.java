package pt.isec.pdrestapi.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static pt.isec.pdrestapi.Application.bd_url;

@Component
public class UserAuthenticationProvider implements AuthenticationProvider
{
    public static boolean admin;
    public static int id_user;
    private Connection dbConn;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException
    {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        if (autenticacaoBD(username,password))
        {
            List<GrantedAuthority> authorities = new ArrayList<>();
            if(admin) {
                authorities.add(new SimpleGrantedAuthority("ADMIN"));
            }else{
                authorities.add(new SimpleGrantedAuthority("USER"));
            }

            return new UsernamePasswordAuthenticationToken(username, password, authorities);
        }

        return null;
    }

    @Override
    public boolean supports(Class<?> authentication)
    {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    public boolean autenticacaoBD(String username,String password){
        boolean status = false;
        try{
            dbConn = DriverManager.getConnection(bd_url);
            Statement statement = dbConn.createStatement();
            String sqlQuery = "SELECT id,username,administrador FROM utilizador WHERE LOWER(username) = LOWER('" + username + "') AND password = '" + password + "'";
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            while (resultSet.next()) //Verifica se ainda tens linhas
            {
                admin = (resultSet.getInt("administrador") == 1);
                id_user = resultSet.getInt("id");
                status = true;
            }
            statement.close();
            resultSet.close();
            dbConn.close();
        }catch (SQLException e){
            e.printStackTrace();
        }

        return status;
    }
}
