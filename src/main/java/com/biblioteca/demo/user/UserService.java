package com.biblioteca.demo.user;

import java.util.List;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.demo.domain.user.UserRole;
import com.biblioteca.demo.infra.exception.BusinessException;
import com.biblioteca.demo.infra.exception.ResourceNotFoundException;
import com.biblioteca.demo.loan.LoanRepository;
import com.biblioteca.demo.refreshtoken.RefreshTokenRepository;
import com.biblioteca.demo.security.TokenService;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoanRepository loanRepository;
    private final TokenService tokenService;
    private final CacheManager cacheManager; // Inject do CacheManager

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
        RefreshTokenRepository refreshTokenRepository, LoanRepository loanRepository, TokenService tokenService,CacheManager cacheManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.loanRepository = loanRepository;
        this.tokenService = tokenService; 
        this.cacheManager = cacheManager;

    }

    // O Spring busca primeiro no Memurai sob a chave 'users::email@exemplo.com'
    @Cacheable(value = "users", key = "#email",unless = "#result == null") 
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

   // 1. DICA DE OURO: Use o namespace "users-dto" para não conflitar com a entidade "User" do Spring Security no Redis!
    @Cacheable(value = "users-dto", key = "#email", unless = "#result == null") 
    @Transactional(readOnly = true)
    public UserResponseDTO findByEmailAuthenticated(String email) {
        User user = userRepository.findByEmail(email);
    
    if (user == null) {
        throw new ResourceNotFoundException("Usuário não encontrado: " + email); 
    }
    
    return toResponseDTO(user);
    }

    public Page<UserResponseDTO> findAllPaged(String search, UserRole role, Pageable pageable) {
        Specification<User> spec = UserSpecifications.withFilter(search, role);
        return userRepository.findAll(spec, pageable).map(this::toResponseDTO);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário não encontrado com o ID: " + id);
        }

        if (loanRepository.existsByUserIdAndReturnedIsFalse(id)) {
            throw new BusinessException("Você precisa devolver seu(s) livro(s) primeiro.");
        }
        loanRepository.deleteByUserId(id);
        refreshTokenRepository.deleteByUserId(id);
        userRepository.deleteById(id);
    }

    public UserResponseDTO createUser(CreateUserDTO dto) {
        // 1. Converte DTO -> Entity
        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        String passwordHash = passwordEncoder.encode(dto.password());
        user.setPassword(passwordHash);
        user.setRole(dto.role());

        // 2. Salva a Entity no PostgreSQL
        User savedUser = userRepository.save(user);

        // 3. Converte a Entity salva -> DTO de Resposta
        return toResponseDTO(savedUser);
    }

 
    /**
     * Limpa o cache usando o #id do parâmetro do método.
     * 
     * Se você tiver mais de uma gaveta/namespace de cache (ex: "users" para a entidade 
     * e "users-dto" para os DTOs do /me), use a anotação @Caching para limpar ambos!
     */
    @Caching(evict = {
        @CacheEvict(value = "users", key = "#id"), 
        @CacheEvict(value = "users-dto", allEntries = true) // Limpa o DTO alterado
    })
    @Transactional
    public UpdateEmailUserResponseDTO patchUser(Long id, PatchUserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o ID: " + id));

        String oldEmail = null;        

        // Atualiza o nome APENAS se o cliente enviou um nome no JSON
        if (dto.name() != null) {
            user.setName(dto.name());
        }  

        // Atualiza o email APENAS se o cliente enviou uma email no JSON
        if (dto.email() != null) {
            // 1. Guarda o e-mail ANTIGO que está no cache do Redis
            oldEmail = user.getEmail();
            user.setEmail(dto.email());
        } 

        if  (oldEmail != null ) { 
            if (cacheManager.getCache("users") != null) {
            cacheManager.getCache("users").evict(oldEmail);
            }
        }
 
        userRepository.save(user);

        // Converte a entidade atualizada para DTO
        UserResponseDTO userDTO = toResponseDTO(user);

        // Gera o novo token com os dados atualizados
        String newToken = tokenService.generateToken(user);

        return new UpdateEmailUserResponseDTO(userDTO, newToken);
    }

    public List<UserResponseDTO> findAll() {
        return userRepository.findAll()
                .stream() // Cria uma "esteira" e bota os livros nela
                .map(this::toResponseDTO) // coloca cada item da esteira em um DTO
                .toList(); // retorna todos os DTOs
    }

    // Método utilitário privado para evitar repetição do mapeamento Entity -> DTO
    private UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail());
    }

    public UserResponseDTO findById(Long id) {
        return userRepository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o ID: " + id));
    }
}
