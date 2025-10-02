using System.ComponentModel.DataAnnotations;

namespace HabitAqui.ViewModels.Funcionarios
{
    public class FuncionarioAddViewModel{

        [EmailAddress]
        public string Email { get; set; }

        [DataType(DataType.Password)]
        public string Password { get; set; }

        public string Role { get; set; }
        public bool Ativo { get; set; }

    }
}
