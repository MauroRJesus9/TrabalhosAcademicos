using HabitAqui.Models;
using Microsoft.AspNetCore.Identity.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore;

namespace HabitAqui.Data
{
    public class ApplicationDbContext : IdentityDbContext<Utilizadores> {

        public DbSet<Categorias> Categorias { get; set; }
        public DbSet<Habitacoes> Habitacoes { get; set; }

        public DbSet<Arrendamentos> Arrendamentos { get; set; }

        public DbSet<Utilizadores> Utilizadores { get; set; }

        public DbSet<HabitacoesFotos> HabitacoesFotos { get; set; }

        public DbSet<HabitacoesRating> HabitacoesRating { get; set; }

        public DbSet<Empresa> Empresa { get; set; }

        public DbSet<EstadoHabitacao> EstadoHabitacao { get; set; }

        public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options)
            : base(options)
        {
        }
    }
}