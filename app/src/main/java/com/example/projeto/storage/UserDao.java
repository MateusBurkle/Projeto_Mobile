package com.example.projeto.storage;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.projeto.models.User;

@Dao
public interface UserDao {

    // Insere um novo usuário.
    // OnConflictStrategy.ABORT faz a inserção falhar se o e-mail já existir
    // (por causa do 'unique = true' que definimos no User.java)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(User user);

    // Busca um usuário pelo e-mail. Usaremos isso no Login.
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User findByEmail(String email);

    // Este método não é mais necessário se fizermos a checagem do hash
    // da senha no código Java, o que é mais seguro.
    // @Query("SELECT * FROM users WHERE email = :email AND senha = :senha LIMIT 1")
    // User findByEmailAndPassword(String email, String senha);
}