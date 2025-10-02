using Microsoft.AspNetCore.Identity;
using HabitAqui.Models;
using SQLitePCL;
using Microsoft.EntityFrameworkCore;

namespace HabitAqui.Data
{
    public enum Roles
    {
        Admin,
        Funcionario,
        Gestor, //locador
        Cliente
    }

    public enum EstadoArrendamento
    {
        Pendente,
        Confirmado,
        Entregue,
        Concluido
    }

    public static class Initialize
    {
        public static async Task CriaDadosIniciais(UserManager<Utilizadores>
       userManager, RoleManager<IdentityRole> roleManager, ApplicationDbContext context)
        {
            //Adicionar default Roles
            await roleManager.CreateAsync(new IdentityRole(Roles.Admin.ToString()));
            await roleManager.CreateAsync(new IdentityRole(Roles.Funcionario.ToString()));
            await roleManager.CreateAsync(new IdentityRole(Roles.Gestor.ToString()));
            await roleManager.CreateAsync(new IdentityRole(Roles.Cliente.ToString()));
            //Adicionar Default User - Admin
            var defaultUser = new Utilizadores
            {
                UserName = "admin@localhost.com",
                Email = "admin@localhost.com",
                NomeCompleto = "Administrador",
                EmailConfirmed = true,
                PhoneNumberConfirmed = true

            };
            var user = await userManager.FindByEmailAsync(defaultUser.Email);
            if (user == null)
            {
                await userManager.CreateAsync(defaultUser, "Admin123@");
                await userManager.AddToRoleAsync(defaultUser,
                Roles.Admin.ToString());
            }

            var defaultUser2 = new Utilizadores
            {
                UserName = "admin@isec.pt",
                Email = "admin@isec.pt",
                NomeCompleto = "Administrador",
                EmailConfirmed = true,
                PhoneNumberConfirmed = true

            };
            var user2 = await userManager.FindByEmailAsync(defaultUser2.Email);
            if (user2 == null)
            {
                await userManager.CreateAsync(defaultUser2, "Admin123_");
                await userManager.AddToRoleAsync(defaultUser2,
                Roles.Admin.ToString());
            }
            var defaultUser3 = new Utilizadores
            {
                UserName = "gest@isec.pt",
                Email = "gest@isec.pt",
                NomeCompleto = "Gestor",
                EmailConfirmed = true,
                PhoneNumberConfirmed = true

            };
            var user3 = await userManager.FindByEmailAsync(defaultUser3.Email);
            if (user3 == null)
            {
                await userManager.CreateAsync(defaultUser3, "Admin123_");
                await userManager.AddToRoleAsync(defaultUser3,
                Roles.Gestor.ToString());
            }


            /*var categorias2 = new Categorias
            {
                Nome = "Casa",
                Disponivel = true
            };
            var categorias3 = new Categorias
            {
                Nome = "Quarto",
                Disponivel = true
            };
            var categorias4 = new Categorias
            {
                Nome = "T1",
                Disponivel = true
            };
            var categorias5 = new Categorias
            {
                Nome = "T2",
                Disponivel = true
            };
            var categorias6 = new Categorias
            {
                Nome = "T3",
                Disponivel = true
            };
            context.Categorias.AddRange(categorias2, categorias3, categorias4, categorias5, categorias6);
            await context.SaveChangesAsync();*/

            /*var empresa = new Empresa
             {
                 Nome = "Empresa1 1",
                 Avaliacao = 4.5,
                 Ativo = true,
             };

             var categorias = new Categorias{
                 Nome = "Apartamento",
                 Disponivel = true
             };
             var categorias2 = new Categorias
             {
                 Nome = "Casa",
                 Disponivel = true
             };

             context.Categorias.AddRange(categorias, categorias2);
             await context.SaveChangesAsync();*/

            /* var habitacao = new Habitacoes{
                 Nome = "Habitação 1",
                 Localidade = "Coimbra",
                 Descricao = "4 Quartos 2 casas de banho",
                 Preco = 100,
                 Categoria = "Apartamento",
                 PeriodoMinimoArrendamento = 1,
                 DataInicio = DateTime.Now,
                 DataFim = DateTime.Now,
                 Estado = "Disponivel",
                 EmpresaId = 1
             };

             var habitacao2 = new Habitacoes
             {
                 Nome = "Habitação 2",
                 Localidade = "Coimbra",
                 Descricao = "4 Quartos 2 casas de banho",
                 Preco = 100,
                 Categoria = "Apartamento",
                 PeriodoMinimoArrendamento = 1,
                 DataInicio = DateTime.Now,
                 DataFim = DateTime.Now,
                 Estado = "Disponivel",
                 EmpresaId = 1
             };

            var habitacao3 = new Habitacoes
            {
                 Nome = "Habitação 3",
                 Localidade = "Coimbra",
                 Descricao = "4 Quartos 2 casas de banho",
                 Preco = 100,
                 Categoria = "Apartamento",
                 PeriodoMinimoArrendamento = 1,
                 DataInicio = DateTime.Now,
                 DataFim = DateTime.Now,
                 Estado = "Disponivel",
                 EmpresaId = 1
             };

             //adicionar as habitacoes a base de dados
             context.Empresa.AddRange(empresa);
             context.Habitacoes.AddRange(habitacao, habitacao2, habitacao3);
             await context.SaveChangesAsync();*/


            /* var habitacaoRating = new HabitacoesRating{
                Comentario = "Comentario 2",
                HabitacoesId = 1,
                UtilizadoresId = "05edaf2a-298f-4eaf-b3fc-a20f1559038b"
            };

            context.HabitacoesRating.AddRange(habitacaoRating);
            await context.SaveChangesAsync(); */

            /*var arrendamentos = new Arrendamentos{
                DataInicio = DateTime.Now,
                DataFim = DateTime.Now,
                HabitacoesId = 3,
                EmpresaId = 3,
                Custo = 100,
                Estado = "Concluida",
                UtilizadoresId = "55d8a049-2898-4ca2-b834-f96b0259c53c"

            };

            context.Arrendamentos.AddRange(arrendamentos);*/
            await context.SaveChangesAsync();


        }
    }
}
